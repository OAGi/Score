package org.oagi.srt.service.treenode.test;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.oagi.srt.repository.AggregateCoreComponentRepository;
import org.oagi.srt.repository.AssociationCoreComponentPropertyRepository;
import org.oagi.srt.repository.entity.AggregateCoreComponent;
import org.oagi.srt.repository.entity.AssociationCoreComponent;
import org.oagi.srt.repository.entity.AssociationCoreComponentProperty;
import org.oagi.srt.service.treenode.AggregateCoreComponentTreeNode;
import org.oagi.srt.service.treenode.AssociationCoreComponentPropertyTreeNode;
import org.oagi.srt.service.treenode.CoreComponentPropertyTreeNode;
import org.oagi.srt.service.treenode.CoreComponentTreeNodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@DataJpaTest
public class CoreComponentTreeNodeTests {

    @Autowired
    private CoreComponentTreeNodeService coreComponentTreeNodeService;

    @Autowired
    private AggregateCoreComponentRepository accRepository;

    @Autowired
    private AssociationCoreComponentPropertyRepository asccpRepository;

    @Test
    public void createAggregateCoreComponentTreeNode() {
        String acknowledgeBOMTypeGuid = "oagis-id-49831133a4ff4586a208398eb5236477";
        AggregateCoreComponent acknowledgeBOMAcc = accRepository.findOneByGuid(acknowledgeBOMTypeGuid);
        assertThat(acknowledgeBOMAcc).isNotNull();

        String businessObjectDocumentTypeGuid = "oagis-id-2783857358f145e799471461f5192fa7";
        // it's the base type of AcknowledgeBOMType
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

        assertThat(accTreeNode.getChildrenCount()).isEqualTo(1);
        Collection<? extends CoreComponentPropertyTreeNode> children = accTreeNode.getChildren();
        assertThat(children).isNotNull();
        assertThat(children.size()).isEqualTo(1);

        CoreComponentPropertyTreeNode child = children.iterator().next();
        assertThat(child).isInstanceOf(AssociationCoreComponentPropertyTreeNode.class);
        assertThat(((AssociationCoreComponentPropertyTreeNode) child).getRaw()).isEqualTo(dataAreaAsccp);
    }
}
