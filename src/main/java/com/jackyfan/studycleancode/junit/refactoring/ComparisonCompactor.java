package com.jackyfan.studycleancode.junit.refactoring;


import com.jackyfan.studycleancode.junit.Assert;

public class ComparisonCompactor {
    private static final String ELLIPSIS = "...";
    private static final String DELTA_END = "]";
    private static final String DELTA_START = "[";
    private int contextLength;
    private String expected;//预期的
    private String actual;//实际的
    private int prefixLength;//优化：加上索引，其实是前缀长度
    private int suffixLength;//优化：加上索引，后缀长度
    private String compactExpected;
    private String compactActual;

    public ComparisonCompactor(int contextLength, String expected, String actual) {
        this.contextLength = contextLength;
        this.expected = expected;
        this.actual = actual;
    }

    /**
     * 函数名很奇怪[N7]。尽管它的确会压缩字符串，但如果 canBeCompact 为false，它实际上就不会压缩字符串。
     * 用compact来命名，隐藏了错误检查的副作用。注意，该函数返回一条格式化后的消息，而不仅仅只是压缩后的字符串。
     * 所以，函数名其实应该是formatCompacted Comparison。在用以下参数调用时，读起来会好很多：
     * public String formatCompactedComparison(String message) {
     * 两个字符串是在if语句体中压缩的。我们应当拆分出一个名为compactExpectedAndActual的方法。
     * 然而，我们希望formatCompactComparison函数完成所有的格式化工作。而compact…函数除了压缩之外什么都不做[G30]。
     * 所以，做如下拆分：
     */
    public String formatCompactedComparison(String message) {
        //优化点：否定式稍微比肯定式难理解一些[
        if (canBeCompacted()) {
            compactExpectedAndActual();
            return Assert.format(message, compactExpected, compactActual);
        } else {
            return Assert.format(message, expected, actual);
        }
    }


    /**
     * private void compactExpectedAndActual() {
     * prefixIndex = findCommonPrefix();
     * //优化点：它依赖于 prefixIndex是由findCommonSuffix计算得来的事实。
     * // 如果这两个方法不是按这样的顺序调用，调试就会变得困难。为了暴露这个时序性耦合，我们将prefixIndex做成find的参数。
     * suffixIndex = findCommonSuffix(prefixIndex);
     * //优化点：函数中的变量会与成员变量同名，改成expected改成compactExpected
     * compactExpected = compactString(expected);
     * compactActual = compactString(actual);
     * }
     */

    private void compactExpectedAndActual() {
        findCommonPrefixAndSuffix();
        //优化点：函数中的变量会与成员变量同名，改成expected改成compactExpected
        compactExpected = compactString(expected);
        compactActual = compactString(actual);
    }

    private boolean canBeCompacted() {
        return expected != null && actual != null && !areStringsEqual();
    }

    private String compactString(String source) {
        return
                computeCommonPrefix() +
                        DELTA_START +
                        source.substring(prefixLength, source.length() - suffixLength) +
                        DELTA_END +
                        computeCommonSuffix();
    }

    private void findCommonPrefix() {
        prefixLength = 0;
        int end = Math.min(expected.length(), actual.length());
        for (; prefixLength < end; prefixLength++) {
            if (expected.charAt(prefixLength) != actual.charAt(prefixLength))
                break;
        }
    }

    private void findCommonPrefixAndSuffix() {
        /**
         * 让它在执行其他操作之前，先调用findCommonPrefix;
         * 这样一来，就以一种相比前种手段更为有效的方式建立了两个函数之间的时序关系;
         */
        findCommonPrefix();
        suffixLength = 0;
        for (; !suffixOverlapsPrefix(suffixLength); suffixLength++) {
            if (charFromEnd(expected, suffixLength) !=
                    charFromEnd(actual, suffixLength))
                break;
        }
    }

    private char charFromEnd(String s, int i) {
        return s.charAt(s.length() - i);
    }

    private boolean suffixOverlapsPrefix(int suffixLength) {
        return actual.length() - suffixLength <= prefixLength || expected.length() - suffixLength <= prefixLength;
    }

    private String computeCommonPrefix() {
        return (prefixLength > contextLength ? ELLIPSIS : "") + expected.substring(Math.max(0, prefixLength - contextLength), prefixLength);
    }

    private String computeCommonSuffix() {
        int end = Math.min(expected.length() - suffixLength + 1 + contextLength, expected.length());
        return expected.substring(expected.length() - suffixLength + 1, end) + (expected.length() - suffixLength + 1 < expected.length() - contextLength ?
                ELLIPSIS : "");
    }

    private boolean areStringsEqual() {
        return expected.equals(actual);
    }
}
