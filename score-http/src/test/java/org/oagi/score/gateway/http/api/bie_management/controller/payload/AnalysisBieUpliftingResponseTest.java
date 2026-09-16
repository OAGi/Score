package org.oagi.score.gateway.http.api.bie_management.controller.payload;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.api.bie_management.model.asbie.Asbie;
import org.oagi.score.gateway.http.api.bie_management.model.asbie.AsbieId;
import org.oagi.score.gateway.http.api.bie_management.model.bbie.Bbie;
import org.oagi.score.gateway.http.api.bie_management.model.bbie.BbieId;
import org.oagi.score.gateway.http.api.bie_management.model.bbie_sc.BbieSc;
import org.oagi.score.gateway.http.api.bie_management.model.bbie_sc.BbieScId;

import java.math.BigInteger;

import static org.assertj.core.api.Assertions.assertThat;

class AnalysisBieUpliftingResponseTest {

    @Test
    void serializesRepeatedMappingsWithTheirDistinctSourcePaths() throws Exception {
        AnalysisBieUpliftingResponse response = new AnalysisBieUpliftingResponse();
        Asbie asbie = new Asbie();
        asbie.setAsbieId(new AsbieId(BigInteger.valueOf(17)));
        Bbie bbie = new Bbie();
        bbie.setBbieId(new BbieId(BigInteger.valueOf(27)));
        BbieSc bbieSc = new BbieSc();
        bbieSc.setBbieScId(new BbieScId(BigInteger.valueOf(37)));

        response.foundBestMatchedAsbie(asbie, null, "source/one", "ctx-one",
                null, "target/one");
        response.foundBestMatchedAsbie(asbie, null, "source/two", "ctx-two",
                null, "target/two");
        response.foundBestMatchedBbie(bbie, null, "bbie/source/one", "ctx-one",
                null, "bbie/target/one");
        response.foundBestMatchedBbie(bbie, null, "bbie/source/two", "ctx-two",
                null, "bbie/target/two");
        response.foundBestMatchedBbieSc(bbieSc, null, "bbie-sc/source/one", "ctx-one",
                null, "bbie-sc/target/one");
        response.foundBestMatchedBbieSc(bbieSc, null, "bbie-sc/source/two", "ctx-two",
                null, "bbie-sc/target/two");

        String json = new ObjectMapper().writeValueAsString(response);

        assertThat(response.getAsbiePathList()).hasSize(2);
        assertThat(response.getBbiePathList()).hasSize(2);
        assertThat(response.getBbieScPathList()).hasSize(2);
        assertThat(json).contains("source/one", "source/two", "target/one", "target/two",
                        "bbie/source/one", "bbie/source/two", "bbie-sc/source/one", "bbie-sc/source/two")
                .doesNotContain("sourceAsbiePathMap", "targetAsbiePathMap",
                        "sourceBbiePathMap", "targetBbiePathMap",
                        "sourceBbieScPathMap", "targetBbieScPathMap");
    }
}
