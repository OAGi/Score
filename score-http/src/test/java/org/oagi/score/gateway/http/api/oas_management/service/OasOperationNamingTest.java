package org.oagi.score.gateway.http.api.oas_management.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.oagi.score.gateway.http.api.oas_management.service.OasOperationNaming.*;

/** The naming rules are those that the clients had before (#1732), so that what a user gets does not change. */
class OasOperationNamingTest {

    @Test
    void mapsVerbsToTheirWords() {
        assertEquals("query", verbWord("GET"));
        assertEquals("delete", verbWord("DELETE"));
        assertEquals("update", verbWord("PATCH"));
        assertEquals("query", verbWord("get"));
        assertEquals("", verbWord("BOGUS"));
        assertEquals("", verbWord(null));
    }

    @Test
    void buildsTheVerbWordTheNameAndTheArrayMarker() {
        assertEquals("deletePurchaseOrder", buildOperationId("DELETE", "Purchase Order", false));
        assertEquals("deletePurchaseOrderList", buildOperationId("DELETE", "Purchase Order", true));
        assertEquals("", buildOperationId("BOGUS", "Purchase Order", true));
    }

    @Test
    void keepsTheArrayMarkerDistinctForANameThatEndsInList() {
        assertEquals("queryPriceList", buildOperationId("GET", "Price List", false));
        assertEquals("queryPriceListList", buildOperationId("GET", "Price List", true));
    }

    @Test
    void recomputesFromThePropertyTermWhenThereIsOne() {
        assertEquals("deletePurchaseOrder", recomputeOperationId("DELETE", "queryPurchaseOrder", false, "Purchase Order"));
        assertEquals("deletePurchaseOrderList", recomputeOperationId("DELETE", "createPurchaseOrderList", true, "Purchase Order"));
        assertEquals("createPriceList", recomputeOperationId("POST", "queryPriceList", false, "Price List"));
        assertEquals("createPriceListList", recomputeOperationId("POST", "queryPriceListList", true, "Price List"));
    }

    @Test
    void recomputesFromTheIdWhenThereIsNoPropertyTerm() {
        assertEquals("updateProductionOrder", recomputeOperationId("PATCH", "deleteProductionOrder", false, null));
        assertEquals("deleteShoppingList", recomputeOperationId("DELETE", "updateShoppingList", false, " "));
    }

    @Test
    void leavesTheIdForAVerbWithoutAWord() {
        assertEquals("queryThing", recomputeOperationId("BOGUS", "queryThing", false, "Thing"));
    }

    @Test
    void extractsTheNameAndCutsTheArrayMarkerOnlyForAnArray() {
        assertEquals("PriceList", extractBieName("queryPriceList", false));
        assertEquals("PriceList", extractBieName("queryPriceListList", true));
        assertEquals("PurchaseOrder", extractBieName("deletePurchaseOrderList", true));
        assertEquals("Order", extractBieName("ctx_createOrder", false));
    }

    @Test
    void buildsTheLeafOfAPathFromThePropertyTerm() {
        assertEquals("purchase-order", resourcePathLeaf("Purchase Order", false));
        assertEquals("purchase-order-list", resourcePathLeaf("Purchase Order", true));
        assertEquals("price-list", resourcePathLeaf("Price List", false));
        assertEquals("price-list-list", resourcePathLeaf("Price List", true));
    }

    @Test
    void buildsAPathWithTheVersionOnlyWhenTheDocumentHasOne() {
        assertEquals("/my-company/1.0/purchase-order", buildResourcePath("My Company", "1.0", "Purchase Order", false));
        assertEquals("/my-company/price-list-list", buildResourcePath("My Company", "", "Price List", true));
        assertEquals("/my-company/price-list-list", buildResourcePath("My Company", null, "Price List", true));
    }

    @Test
    void makesTheMarkerOfAPathFollowTheArrayIndicatorByItsLastSegmentOnly() {
        assertEquals("/bc/1.0/price-list-list", applyResourceArrayMarker("/bc/1.0/price-list", "Price List", true));
        assertEquals("/bc/1.0/price-list", applyResourceArrayMarker("/bc/1.0/price-list-list", "Price List", false));
        assertEquals("/bc/1.0/purchase-order-list", applyResourceArrayMarker("/bc/1.0/purchase-order", "Purchase Order", true));
        assertEquals("purchase-order-list", applyResourceArrayMarker("purchase-order", "Purchase Order", true));
    }

    @Test
    void leavesAPathThatEndsInAPathParameterAsItIs() {
        assertEquals("/containers/{id}", applyResourceArrayMarker("/containers/{id}", "Container", true));
        assertEquals("/containers/{id}", applyResourceArrayMarker("/containers/{id}", "Container", false));
    }

    @Test
    void makesTheOperationIdOfAnOperationWithoutABieFromItsPath() {
        assertEquals("updateProductionOrder", operationIdFromPath("PATCH", "/production-order/{id}"));
        assertEquals("deleteShoppingCart", operationIdFromPath("DELETE", "/shop/shopping_cart/{id}/"));
        assertEquals("", operationIdFromPath("DELETE", "/{id}"));
        assertEquals("", operationIdFromPath("", "/orders"));
    }

    @Test
    void acceptsAPathAndSaysWhyAnotherIsNone() {
        assertTrue(resourcePathError("/orders/{id}/lines").isEmpty());
        assertTrue(resourcePathError("").isEmpty()); // for the check that it is given
        assertEquals("Path must start with \"/\".", resourcePathError("orders").orElseThrow());
        assertEquals("Path must not contain whitespace.", resourcePathError("/or ders").orElseThrow());
        assertEquals("Path must not contain a query string (\"?\") or fragment (\"#\").", resourcePathError("/orders?x=1").orElseThrow());
        assertEquals("Path has unbalanced \"{\" and \"}\".", resourcePathError("/orders/{id").orElseThrow());
        assertEquals("Path has unbalanced \"{\" and \"}\".", resourcePathError("/orders/id}").orElseThrow());
        assertEquals("Path parameter \"{}\" must not be empty.", resourcePathError("/orders/{}").orElseThrow());
    }
}
