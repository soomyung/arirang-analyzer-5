package org.apache.lucene.ko;

import org.apache.commons.lang.StringUtils;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.ko.KoreanAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import junit.framework.TestCase;

public class TestKoreanAnalyzer extends TestCase {

	public void testKoreanAnalyzer() throws Exception {
		
		String input = "정식시호는 명헌숙경예인정목홍성장순정휘장소단희수현의헌강수유령자온공안효정왕후(明憲淑敬睿仁正穆弘聖章純貞徽莊昭端禧粹顯懿獻康綏裕寧慈溫恭安孝定王后)이며 돈령부영사(敦寧府領事) 홍재룡(洪在龍)의 딸이다. 1844년, 헌종의 정비(正妃)인 효현왕후가 승하하자 헌종의 계비로써 중궁에 책봉되었으나 5년 뒤인 1849년에 남편 헌종이 승하하고 철종이 즉위하자 19세의 어린 나이로 대비가 되었다. 1857년 시조모 대왕대비 순원왕후가 승하하자 왕대비가 되었다.";
//		input = "정식시호는 明憲淑敬睿";
//		input = "정보화용역사업";
//		input = "空間의";
//		input =  "di" + '\u000B' + "erent";
//		input = "찾아서- C# 달리기";
		
		KoreanAnalyzer a = new KoreanAnalyzer();
		a.setQueryMode(false);
		
		StringBuilder actual = new StringBuilder();
		
	     TokenStream ts = a.tokenStream("bogus", input);
	      CharTermAttribute termAtt = ts.addAttribute(CharTermAttribute.class);
	      ts.reset();
	      while (ts.incrementToken()) {
	        actual.append(termAtt.toString());
	        actual.append(' ');
	      }
	      System.out.println(actual);
	     
//	      assertEquals("for line " + line + " input: " + input, expected, actual.toString());
	      ts.end();
	      ts.close();
	}
	
	public void testConvertUnicode() throws Exception {
		char c = 0x772C;
		System.out.println(c);
		
		int code = '領';
		System.out.println(code);
		
		System.out.println(Character.getType('&'));
	}
}
