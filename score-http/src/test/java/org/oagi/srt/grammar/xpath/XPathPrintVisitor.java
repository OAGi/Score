package org.oagi.srt.grammar.xpath;

//import org.antlr.v4.runtime.tree.AbstractParseTreeVisitor;

public class XPathPrintVisitor<T> {
//        extends AbstractParseTreeVisitor<T> implements XPathVisitor<T> {
//    @Override
//    public T visitMain(XPathParser.MainContext ctx) {
//        System.out.println("visitMain(XPathParser.MainContext): " + ctx);
//        return visitChildren(ctx);
//    }
//
//    @Override
//    public T visitLocationPath(XPathParser.LocationPathContext ctx) {
//        System.out.println("visitLocationPath(XPathParser.LocationPathContext): " + ctx);
//        return visitChildren(ctx);
//    }
//
//    @Override
//    public T visitAbsoluteLocationPathNoroot(XPathParser.AbsoluteLocationPathNorootContext ctx) {
//        System.out.println("visitAbsoluteLocationPathNoroot(XPathParser.AbsoluteLocationPathNorootContext): " + ctx);
//        return visitChildren(ctx);
//    }
//
//    @Override
//    public T visitRelativeLocationPath(XPathParser.RelativeLocationPathContext ctx) {
//        System.out.println("visitRelativeLocationPath(XPathParser.RelativeLocationPathContext): " + ctx);
//        return visitChildren(ctx);
//    }
//
//    @Override
//    public T visitStep(XPathParser.StepContext ctx) {
//        System.out.println("visitStep(XPathParser.StepContext): " + ctx);
//        return visitChildren(ctx);
//    }
//
//    @Override
//    public T visitAxisSpecifier(XPathParser.AxisSpecifierContext ctx) {
//        System.out.println("visitAxisSpecifier(XPathParser.AxisSpecifierContext): " + ctx);
//        return visitChildren(ctx);
//    }
//
//    @Override
//    public T visitNodeTest(XPathParser.NodeTestContext ctx) {
//        System.out.println("visitNodeTest(XPathParser.NodeTestContext): " + ctx);
//        return visitChildren(ctx);
//    }
//
//    @Override
//    public T visitPredicate(XPathParser.PredicateContext ctx) {
//        System.out.println("visitPredicate(XPathParser.PredicateContext): " + ctx);
//        return visitChildren(ctx);
//    }
//
//    @Override
//    public T visitAbbreviatedStep(XPathParser.AbbreviatedStepContext ctx) {
//        System.out.println("visitAbbreviatedStep(XPathParser.AbbreviatedStepContext): " + ctx);
//        return visitChildren(ctx);
//    }
//
//    @Override
//    public T visitExpr(XPathParser.ExprContext ctx) {
//        System.out.println("visitExpr(XPathParser.ExprContext): " + ctx);
//        return visitChildren(ctx);
//    }
//
//    @Override
//    public T visitPrimaryExpr(XPathParser.PrimaryExprContext ctx) {
//        System.out.println("visitPrimaryExpr(XPathParser.PrimaryExprContext): " + ctx);
//        return visitChildren(ctx);
//    }
//
//    @Override
//    public T visitFunctionCall(XPathParser.FunctionCallContext ctx) {
//        System.out.println("visitFunctionCall(XPathParser.FunctionCallContext): " + ctx);
//        return visitChildren(ctx);
//    }
//
//    @Override
//    public T visitUnionExprNoRoot(XPathParser.UnionExprNoRootContext ctx) {
//        System.out.println("visitUnionExprNoRoot(XPathParser.UnionExprNoRootContext): " + ctx);
//        return visitChildren(ctx);
//    }
//
//    @Override
//    public T visitPathExprNoRoot(XPathParser.PathExprNoRootContext ctx) {
//        System.out.println("visitPathExprNoRoot(XPathParser.PathExprNoRootContext): " + ctx);
//        return visitChildren(ctx);
//    }
//
//    @Override
//    public T visitFilterExpr(XPathParser.FilterExprContext ctx) {
//        System.out.println("visitFilterExpr(XPathParser.FilterExprContext): " + ctx);
//        return visitChildren(ctx);
//    }
//
//    @Override
//    public T visitOrExpr(XPathParser.OrExprContext ctx) {
//        System.out.println("visitOrExpr(XPathParser.OrExprContext): " + ctx);
//        return visitChildren(ctx);
//    }
//
//    @Override
//    public T visitAndExpr(XPathParser.AndExprContext ctx) {
//        System.out.println("visitAndExpr(XPathParser.AndExprContext): " + ctx);
//        return visitChildren(ctx);
//    }
//
//    @Override
//    public T visitEqualityExpr(XPathParser.EqualityExprContext ctx) {
//        System.out.println("visitEqualityExpr(XPathParser.EqualityExprContext): " + ctx);
//        return visitChildren(ctx);
//    }
//
//    @Override
//    public T visitRelationalExpr(XPathParser.RelationalExprContext ctx) {
//        System.out.println("visitRelationalExpr(XPathParser.RelationalExprContext): " + ctx);
//        return visitChildren(ctx);
//    }
//
//    @Override
//    public T visitAdditiveExpr(XPathParser.AdditiveExprContext ctx) {
//        System.out.println("visitAdditiveExpr(XPathParser.AdditiveExprContext): " + ctx);
//        return visitChildren(ctx);
//    }
//
//    @Override
//    public T visitMultiplicativeExpr(XPathParser.MultiplicativeExprContext ctx) {
//        System.out.println("visitMultiplicativeExpr(XPathParser.MultiplicativeExprContext): " + ctx);
//        return visitChildren(ctx);
//    }
//
//    @Override
//    public T visitUnaryExprNoRoot(XPathParser.UnaryExprNoRootContext ctx) {
//        System.out.println("visitUnaryExprNoRoot(XPathParser.UnaryExprNoRootContext): " + ctx);
//        System.out.println("ctx.MINUS(): " + ctx.MINUS());
//        return visitChildren(ctx);
//    }
//
//    @Override
//    public T visitQName(XPathParser.QNameContext ctx) {
//        System.out.println("visitQName(XPathParser.QNameContext): " + ctx);
//        System.out.println("ctx.COLON(): " + ctx.COLON());
//        return visitChildren(ctx);
//    }
//
//    @Override
//    public T visitFunctionName(XPathParser.FunctionNameContext ctx) {
//        System.out.println("visitFunctionName(XPathParser.FunctionNameContext): " + ctx);
//        System.out.println("ctx.AxisName(): " + ctx.AxisName());
//        System.out.println("ctx.COLON(): " + ctx.COLON());
//        System.out.println("ctx.NCName(): " + ctx.NCName());
//        return visitChildren(ctx);
//    }
//
//    @Override
//    public T visitVariableReference(XPathParser.VariableReferenceContext ctx) {
//        System.out.println("visitVariableReference(XPathParser.VariableReferenceContext): " + ctx);
//        return visitChildren(ctx);
//    }
//
//    @Override
//    public T visitNameTest(XPathParser.NameTestContext ctx) {
//        System.out.println("visitNameTest(XPathParser.NameTestContext): " + ctx);
//        System.out.println("ctx.MUL(): " + ctx.MUL());
//        System.out.println("ctx.COLON(): " + ctx.COLON());
//        return visitChildren(ctx);
//    }
//
//    @Override
//    public T visitNCName(XPathParser.NCNameContext ctx) {
//        System.out.println("visitNCName(XPathParser.NCNameContext): " + ctx);
//        System.out.println("ctx.NCName(): " + ctx.NCName());
//        return visitChildren(ctx);
//    }
}
