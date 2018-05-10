package name.zicat.solr.plugin;

import java.io.IOException;
import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;

public class PinyinTransformTokenFilter extends TokenFilter {
    private boolean isOutChinese = true;
    private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
    private final PositionIncrementAttribute positionIncrementAttribute = addAttribute(PositionIncrementAttribute.class);
    HanyuPinyinOutputFormat outputFormat = new HanyuPinyinOutputFormat();
    private final int _minTermLenght;

    private boolean startPinYin = false;
    private String[] pinyinTerm = new String[2];
    private int pinyinIndex = 0;

    public PinyinTransformTokenFilter(TokenStream input) {
        this(input, 2);
    }
    public PinyinTransformTokenFilter(TokenStream input, int minTermLenght) {
        super(input);
        this._minTermLenght = minTermLenght;
        this.outputFormat.setCaseType(HanyuPinyinCaseType.LOWERCASE);
        this.outputFormat.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
    }

    public static boolean containsChinese(String s) {
        if ((null == s) || ("".equals(s.trim())))
            return false;
        for (int i = 0; i < s.length(); i++) {
            if (isChinese(s.charAt(i)))
                return true;
        }
        return false;
    }

    public static boolean isChinese(char a) {
        int v = a;
        return (v >= 19968) && (v <= 171941);
    }

    public final boolean incrementToken() throws IOException {
        if(startPinYin && pinyinIndex < 2) {
            this.termAtt.copyBuffer(pinyinTerm[pinyinIndex].toCharArray(), 0, pinyinTerm[pinyinIndex].length());
            this.positionIncrementAttribute.setPositionIncrement(0);
            pinyinIndex++;
            if(pinyinIndex >= 2) {
                startPinYin = false;
                pinyinIndex = 0;
            }
            return true;
        }
        if (this.input.incrementToken()) {
            String chinese = this.termAtt.toString();
            try {
                if (containsChinese(chinese) && chinese.length() >= this._minTermLenght) {
                    getPyString(chinese);
                    startPinYin = true;
                }
            } catch (BadHanyuPinyinOutputFormatCombination badHanyuPinyinOutputFormatCombination) {
                badHanyuPinyinOutputFormatCombination.printStackTrace();
            }
            return true;
        } else {
            return false;
        }
    }

    public void reset() throws IOException {
        super.reset();
        startPinYin = false;
        pinyinIndex = 0;
    }

    private void getPyString(String chinese) throws BadHanyuPinyinOutputFormatCombination {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < chinese.length(); i++) {
            String[] array = PinyinHelper.toHanyuPinyinStringArray(chinese.charAt(i), this.outputFormat);
            if ((array == null) || (array.length == 0)) {
                continue;
            }
            String s = array[0];
            char c = s.charAt(0);

            sb.append(c);
        }
        pinyinTerm[0] = sb.toString();
        pinyinTerm[1] = PinyinHelper.toHanyuPinyinString(chinese, this.outputFormat, "");
    }
}