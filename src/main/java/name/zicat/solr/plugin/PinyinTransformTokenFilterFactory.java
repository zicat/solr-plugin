package name.zicat.solr.plugin;

import java.util.Map;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.util.TokenFilterFactory;

/**
 *
 */
public class PinyinTransformTokenFilterFactory extends TokenFilterFactory {

    private boolean isFirstChar = false;
    private boolean isOutChinese = true;
    private int minTermLenght = 2;

    public PinyinTransformTokenFilterFactory(Map<String, String> args) {
        super(args);
        this.minTermLenght = getInt(args, "minTermLenght", 2);

        if (!args.isEmpty())
            throw new IllegalArgumentException("Unknown parameters: " + args);
    }

    public TokenFilter create(TokenStream input) {
        return new PinyinTransformTokenFilter(input, this.minTermLenght);
    }
}