package org.oagi.srt.service.test;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.oagi.srt.model.treenode.AssociationBusinessInformationEntityPropertyTreeNode;
import org.oagi.srt.model.treenode.BasicBusinessInformationEntityPropertyTreeNode;
import org.oagi.srt.model.treenode.BusinessInformationEntityTreeNode;
import org.oagi.srt.repository.*;
import org.oagi.srt.repository.entity.AssociationCoreComponentProperty;
import org.oagi.srt.repository.entity.BusinessContext;
import org.oagi.srt.service.TreeNodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@DataJpaTest
public class BusinessInformationEntityTreeNodeTests {

    @Autowired
    private TreeNodeService treeNodeService;

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

    /**
     * [ Model/BODs/AcknowledgePurchaseOrder.xsd ]
     *
     * <xsd:element name="AcknowledgePurchaseOrder" type="AcknowledgePurchaseOrderType" id="oagis-id-153587c9a1ba4b27bc594184dc377182"/>
     *
     * <xsd:complexType name="AcknowledgePurchaseOrderType" id="oagis-id-e8f0eb8c5d7241f4aadde7d7f9d45169">
     *     <xsd:complexContent>
     *         <xsd:extension base="BusinessObjectDocumentType">
     *             <xsd:sequence>
     *                 <xsd:element name="DataArea" type="AcknowledgePurchaseOrderDataAreaType" id="oagis-id-5e5d4c16841548cda5efa87ef1658822"/>
     *             </xsd:sequence>
     *         </xsd:extension>
     *     </xsd:complexContent>
     * </xsd:complexType>
     *
     * [ Model/Platform/2_2/Common/Components/Meta.xsd ]
     *
     * <xsd:complexType name="BusinessObjectDocumentType" id="oagis-id-2783857358f145e799471461f5192fa7">
     *     <xsd:sequence>
     *         <xsd:element ref="ApplicationArea" id="oagis-id-c2cb6823837d4149b32aefb8fd4120cd"/>
     *     </xsd:sequence>
     *     <xsd:attribute name="releaseID" type="NormalizedStringType" use="required" id="oagis-id-0e403050beea4692a5b92eacf5c81b41"/>
     *     <xsd:attribute name="versionID" type="NormalizedStringType" use="optional" id="oagis-id-57d07de3a9f842869240c629ad0127b6"/>
     *     <xsd:attribute name="systemEnvironmentCode" type="SystemEnvironmentCodeContentType" use="optional" default="Production" id="oagis-id-0be42556ff8d4d679ca7658169ab3d0c"/>
     *     <xsd:attribute name="languageCode" type="LanguageCodeContentType" use="optional" default="en-US" id="oagis-id-c0e5355ae62649a4b5e00fdc79144568"/>
     * </xsd:complexType>
     *
     * <xsd:element name="ApplicationArea" type="ApplicationAreaType" id="oagis-id-e8f1f16759e440c2911522aaee3ae97c"/>
     *
     * 1: releaseID,
     * 2: versionID,
     * 3: systemEnvironmentCode,
     * 4: languageCode,
     * 5: ApplicationArea,
     * 6: DataArea
     */
    @Test
    public void createAggregateCoreComponentTreeNode() {
        AssociationBusinessInformationEntityPropertyTreeNode acknowledgePurchaseOrderTreeNode =
                createAcknowledgePurchaseOrderTreeNode();

        Collection<? extends BusinessInformationEntityTreeNode> children = acknowledgePurchaseOrderTreeNode.getChildren();
        assertThat(children.size()).isEqualTo(6);
    }

    @Test
    public void orderingOfSequenceTest() {
        AssociationBusinessInformationEntityPropertyTreeNode acknowledgePurchaseOrderTreeNode =
                createAcknowledgePurchaseOrderTreeNode();

        Collection<? extends BusinessInformationEntityTreeNode> children = acknowledgePurchaseOrderTreeNode.getChildren();

        int index = 0;
        Map<Integer, String> indexGuidMap = new HashMap();
        indexGuidMap.put(index++, "oagis-id-0e403050beea4692a5b92eacf5c81b41");
        indexGuidMap.put(index++, "oagis-id-57d07de3a9f842869240c629ad0127b6");
        indexGuidMap.put(index++, "oagis-id-0be42556ff8d4d679ca7658169ab3d0c");
        indexGuidMap.put(index++, "oagis-id-c0e5355ae62649a4b5e00fdc79144568");
        indexGuidMap.put(index++, "oagis-id-c2cb6823837d4149b32aefb8fd4120cd");
        indexGuidMap.put(index++, "oagis-id-5e5d4c16841548cda5efa87ef1658822");

        index = 0;
        for (BusinessInformationEntityTreeNode child : children) {
            if (child instanceof AssociationBusinessInformationEntityPropertyTreeNode) {
                AssociationBusinessInformationEntityPropertyTreeNode asbiepNode =
                        (AssociationBusinessInformationEntityPropertyTreeNode) child;
                assertThat(indexGuidMap.get(index++)).isEqualTo(asbiepNode.getAssociationCoreComponent().getGuid());
            } else if (child instanceof BasicBusinessInformationEntityPropertyTreeNode) {
                BasicBusinessInformationEntityPropertyTreeNode bbiepNode =
                        (BasicBusinessInformationEntityPropertyTreeNode) child;
                assertThat(indexGuidMap.get(index++)).isEqualTo(bbiepNode.getBasicCoreComponent().getGuid());
            }
        }
    }

    private AssociationBusinessInformationEntityPropertyTreeNode createAcknowledgePurchaseOrderTreeNode() {
        String acknowledgePurchaseOrderAsccpGuid = "oagis-id-153587c9a1ba4b27bc594184dc377182";
        AssociationCoreComponentProperty acknowledgePurchaseOrderAsccp =
                asccpRepository.findOneByGuid(acknowledgePurchaseOrderAsccpGuid);
        assertThat(acknowledgePurchaseOrderAsccp).isNotNull();
        BusinessContext bizCtx = new BusinessContext();

        AssociationBusinessInformationEntityPropertyTreeNode acknowledgePurchaseOrderTreeNode =
                treeNodeService.createBusinessInformationEntityTreeNode(acknowledgePurchaseOrderAsccp, bizCtx);
        assertThat(acknowledgePurchaseOrderTreeNode).isNotNull();
        assertThat(acknowledgePurchaseOrderTreeNode.hasChild()).isEqualTo(true);

        return acknowledgePurchaseOrderTreeNode;
    }

}
