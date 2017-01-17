package org.oagi.srt.service.treenode.test;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.oagi.srt.repository.*;
import org.oagi.srt.repository.entity.*;
import org.oagi.srt.service.treenode.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@DataJpaTest
public class CoreComponentTreeNodeTests {

    @Autowired
    private CoreComponentTreeNodeService coreComponentTreeNodeService;

    @Autowired
    private BasicCoreComponentRepository bccRepository;

    @Autowired
    private BasicCoreComponentPropertyRepository bccpRepository;

    @Autowired
    private AggregateCoreComponentRepository accRepository;

    @Autowired
    private AssociationCoreComponentRepository asccRepository;

    @Autowired
    private AssociationCoreComponentPropertyRepository asccpRepository;

    /*
     * [ Model/BODs/AcknowledgeBOM.xsd ]
     *
     * <xsd:complexType name="AcknowledgeBOMType" id="oagis-id-49831133a4ff4586a208398eb5236477">
     *     <xsd:complexContent>
     *         <xsd:extension base="BusinessObjectDocumentType">
     *             <xsd:sequence>
     *                 <xsd:element name="DataArea" type="AcknowledgeBOMDataAreaType" id="oagis-id-cf2c1df2881a434f8cce0855c301a0c8"/>
     *             </xsd:sequence>
     *         </xsd:extension>
     *     </xsd:complexContent>
     * </xsd:complexType>
     */
    @Test
    public void createAggregateCoreComponentTreeNode() {
        String acknowledgeBOMTypeGuid = "oagis-id-49831133a4ff4586a208398eb5236477";
        AggregateCoreComponent acknowledgeBOMAcc = accRepository.findOneByGuid(acknowledgeBOMTypeGuid);
        assertThat(acknowledgeBOMAcc).isNotNull();

        String businessObjectDocumentTypeGuid = "oagis-id-2783857358f145e799471461f5192fa7";
        // it's the 'BusinessObjectDocumentType' base type of AcknowledgeBOMType
        AggregateCoreComponent businessObjectDocumentTypeAcc = accRepository.findOneByGuid(businessObjectDocumentTypeGuid);
        assertThat(businessObjectDocumentTypeAcc).isNotNull();

        String dataTypeGuid = "oagis-id-cf2c1df2881a434f8cce0855c301a0c8";
        // it's the 'DataArea' ASCCP of AcknowledgeBOMType
        AssociationCoreComponentProperty dataAreaAsccp = asccpRepository.findOneByGuid(dataTypeGuid);
        assertThat(dataAreaAsccp).isNotNull();

        AggregateCoreComponentTreeNode accTreeNode =
                coreComponentTreeNodeService.createCoreComponentTreeNode(acknowledgeBOMAcc);
        assertThat(accTreeNode).isNotNull();
        assertThat(accTreeNode.getRaw()).isEqualTo(acknowledgeBOMAcc);

        AggregateCoreComponentTreeNode baseAccTreeNode = accTreeNode.getBase();
        assertThat(baseAccTreeNode).isNotNull();
        assertThat(baseAccTreeNode.getRaw()).isEqualTo(businessObjectDocumentTypeAcc);

        Collection<? extends CoreComponentPropertyTreeNode> children = accTreeNode.getChildren();
        assertThat(children).isNotNull();
        assertThat(children.size()).isEqualTo(1);

        CoreComponentPropertyTreeNode child = children.iterator().next();
        assertThat(child).isInstanceOf(AssociationCoreComponentPropertyTreeNode.class);
        assertThat(((AssociationCoreComponentPropertyTreeNode) child).getRaw()).isEqualTo(dataAreaAsccp);
    }

    /*
     * [ Model/BODs/AcknowledgeBOM.xsd ]
     *
     * <xsd:complexType name="AcknowledgeBOMDataAreaType" id="oagis-id-c9e79fe541bf4e25be70f80abe4e3145">
     *     <xsd:sequence>
     *         <xsd:element ref="Acknowledge" id="oagis-id-f8b4f55ad21d471dbbe204a156e520f7"/>
     *         <xsd:element ref="BOM" maxOccurs="unbounded" id="oagis-id-0cdae06e26314915b44fd03bc405582e"/>
     *     </xsd:sequence>
     * </xsd:complexType>
     */
    @Test
    public void createAssociationCoreComponentPropertyTreeNode() {
        String acknowledgeBOMDataAreaTypeGuid = "oagis-id-c9e79fe541bf4e25be70f80abe4e3145";
        AggregateCoreComponent acknowledgeBOMDataAreaTypeAcc = accRepository.findOneByGuid(acknowledgeBOMDataAreaTypeGuid);
        assertThat(acknowledgeBOMDataAreaTypeAcc).isNotNull();

        List<AssociationCoreComponent> asccList =
                asccRepository.findByFromAccId(acknowledgeBOMDataAreaTypeAcc.getAccId());
        assertThat(asccList.size()).isEqualTo(2);
        Collections.sort(asccList, Comparator.comparingInt(AssociationCoreComponent::getSeqKey));

        // First ASCC, 'Acknowledge'
        AssociationCoreComponent acknowledgeAscc = asccList.get(0);
        AssociationCoreComponentPropertyTreeNode acknowledgeAsccTreeNode =
                coreComponentTreeNodeService.createCoreComponentTreeNode(acknowledgeAscc);
        assertThat(acknowledgeAsccTreeNode).isNotNull();
        assertThat(acknowledgeAsccTreeNode.getParent().getRaw()).isEqualTo(acknowledgeBOMDataAreaTypeAcc);

        String acknowledgeAsccpGuid = "oagis-id-d9589936aace44f5bd890ec462888263";
        AssociationCoreComponentProperty acknowledgeAsccp = asccpRepository.findOneByGuid(acknowledgeAsccpGuid);
        assertThat(acknowledgeAsccTreeNode.getRaw()).isEqualTo(acknowledgeAsccp);

        // Second ASCC, 'BOM'
        AssociationCoreComponent bomAscc = asccList.get(1);
        AssociationCoreComponentPropertyTreeNode bomAsccTreeNode =
                coreComponentTreeNodeService.createCoreComponentTreeNode(bomAscc);
        assertThat(bomAsccTreeNode).isNotNull();

        String bomAsccpGuid = "oagis-id-465ed46fd9a4422186327a77ed3b4fbf";
        AssociationCoreComponentProperty bomAsccp = asccpRepository.findOneByGuid(bomAsccpGuid);
        assertThat(bomAsccTreeNode.getRaw()).isEqualTo(bomAsccp);
        assertThat(bomAsccTreeNode.getParent().getRaw()).isEqualTo(acknowledgeBOMDataAreaTypeAcc);
    }

    /*
     * <xsd:complexType name="BusinessObjectDocumentType" id="oagis-id-2783857358f145e799471461f5192fa7">
	 *     <xsd:sequence>
	 *         <xsd:element ref="ApplicationArea" id="oagis-id-c2cb6823837d4149b32aefb8fd4120cd"/
	 *     </xsd:sequence>
	 *     <xsd:attribute name="releaseID" type="NormalizedStringType" use="required" id="oagis-id-0e403050beea4692a5b92eacf5c81b41"/>
	 *     <xsd:attribute name="versionID" type="NormalizedStringType" use="optional" id="oagis-id-57d07de3a9f842869240c629ad0127b6"/>
	 *     <xsd:attribute name="systemEnvironmentCode" type="SystemEnvironmentCodeContentType" use="optional" default="Production" id="oagis-id-0be42556ff8d4d679ca7658169ab3d0c"/>
	 *     <xsd:attribute name="languageCode" type="LanguageCodeContentType" use="optional" default="en-US" id="oagis-id-c0e5355ae62649a4b5e00fdc79144568"/>
	 * </xsd:complexType>
     */
    @Test
    public void createBasicCoreComponentPropertyTreeNode() {
        String businessObjectDocumentTypeGuid = "oagis-id-2783857358f145e799471461f5192fa7";
        AggregateCoreComponent businessObjectDocumentTypeAcc = accRepository.findOneByGuid(businessObjectDocumentTypeGuid);
        assertThat(businessObjectDocumentTypeAcc).isNotNull();

        List<BasicCoreComponent> bccList =
                bccRepository.findByFromAccId(businessObjectDocumentTypeAcc.getAccId());
        assertThat(bccList.size()).isEqualTo(4);
        Collections.sort(bccList, coreComponentTreeNodeService.comparingCoreComponentRelation());

        AggregateCoreComponentTreeNode businessObjectDocumentTreeNode =
                coreComponentTreeNodeService.createCoreComponentTreeNode(businessObjectDocumentTypeAcc);
        assertThat(businessObjectDocumentTreeNode).isNotNull();

        Collection<? extends CoreComponentPropertyTreeNode> bccpTreeNodeChildren =
                businessObjectDocumentTreeNode.getChildren().stream()
                        .filter(e -> e instanceof BasicCoreComponentPropertyTreeNode)
                        .collect(Collectors.toList());
        assertThat(bccpTreeNodeChildren.size()).isEqualTo(4);

        Map<String, BasicCoreComponent> bccGuidMap =
                bccList.stream().collect(Collectors.toMap(e -> e.getGuid(), Function.identity()));
        Map<String, CoreComponentPropertyTreeNode> bccTreeNodeGuidMap =
                bccpTreeNodeChildren.stream().collect(Collectors.toMap(e -> e.getId(), Function.identity()));

        assertBccpTreeNode(bccGuidMap, bccTreeNodeGuidMap, "oagis-id-0e403050beea4692a5b92eacf5c81b41");
        assertBccpTreeNode(bccGuidMap, bccTreeNodeGuidMap, "oagis-id-57d07de3a9f842869240c629ad0127b6");
        assertBccpTreeNode(bccGuidMap, bccTreeNodeGuidMap, "oagis-id-0be42556ff8d4d679ca7658169ab3d0c");
        assertBccpTreeNode(bccGuidMap, bccTreeNodeGuidMap, "oagis-id-c0e5355ae62649a4b5e00fdc79144568");
    }

    private void assertBccpTreeNode(Map<String, BasicCoreComponent> bccGuidMap,
                                    Map<String, CoreComponentPropertyTreeNode> bccTreeNodeGuidMap,
                                    String guid) {
        BasicCoreComponent bcc = bccGuidMap.get(guid);
        assertThat(bcc).isNotNull();

        BasicCoreComponentProperty bccp = bccpRepository.findOne(bcc.getToBccpId());
        assertThat(bccp).isNotNull();

        CoreComponentPropertyTreeNode bccpTreeNode = bccTreeNodeGuidMap.get(bccp.getGuid());
        assertThat(bccpTreeNode).isNotNull();
        assertThat(bccpTreeNode.getRaw()).isEqualTo(bccp);
        assertThat(bccpTreeNode.getRawRelation()).isEqualTo(bcc);
    }

    /*
     * [ Model/Platform/2_2/Common/Components/Meta.xsd ]
     *
     * <xsd:complexType name="StateChangeBaseType" id="oagis-id-51533a96aced4db8ab6c6285e9fb5d9a">
     *     <xsd:complexContent>
     *         <xsd:extension base="IdentificationType">
     *             <xsd:sequence>
     *                 <xsd:element ref="FromStateCode" id="oagis-id-52641cf6bae64b05b84bebe1a55bc6b1" minOccurs="0"/>
     *                 <xsd:element ref="ToStateCode" id="oagis-id-e116f4140c514850a9873bfc582c8418" minOccurs="0"/>
     *                 <xsd:element ref="ChangeDateTime" id="oagis-id-df798c8c4e2c455890d75625988d35bb" minOccurs="0"/>
     *                 <xsd:group ref="FreeFormTextGroup" id="oagis-id-d10e8dff43c44342b8819671c9a85ce3" minOccurs="0"/>
     *             </xsd:sequence>
     *         </xsd:extension>
     *     </xsd:complexContent>
     * </xsd:complexType>
     *
     * <xsd:group name="FreeFormTextGroup" id="oagis-id-5aa1636ad48544199515204796b77951">
     *     <xsd:sequence>
     *         <xsd:element ref="Description" id="oagis-id-4dc2f9f93d4540bebabf63ded72b7d2c" minOccurs="0" maxOccurs="unbounded"/>
     *         <xsd:element ref="Note" id="oagis-id-41ef1010201645918eefa5093cb20583" minOccurs="0" maxOccurs="unbounded"/>
     *     </xsd:sequence>
     * </xsd:group>
     *
     * The 'StateChangeBaseType' ACC should have five(5) ASCCs as a children.
     */
    @Test
    public void includeGroupElementsTest() {
        String stateChangeBaseTypeGuid = "oagis-id-51533a96aced4db8ab6c6285e9fb5d9a";
        AggregateCoreComponent stateChangeBaseTypeAcc = accRepository.findOneByGuid(stateChangeBaseTypeGuid);
        assertThat(stateChangeBaseTypeAcc).isNotNull();

        AggregateCoreComponentTreeNode stateChangeBaseTypeTreeNode =
                coreComponentTreeNodeService.createCoreComponentTreeNode(stateChangeBaseTypeAcc);
        assertThat(stateChangeBaseTypeTreeNode).isNotNull();

        Collection<? extends CoreComponentPropertyTreeNode> children = stateChangeBaseTypeTreeNode.getChildren();
        assertThat(children.size()).isEqualTo(5);
    }
}
