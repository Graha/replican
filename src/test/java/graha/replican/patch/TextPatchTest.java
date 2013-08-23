package graha.replican.patch;

import junit.framework.TestCase;

import java.util.LinkedList;
import graha.replican.patch.TextPatch.Diff;
import graha.replican.patch.TextPatch.Patch;
/**
 * <b>about</b>
 *
 * @author graha
 * @created 8/22/13 1:36 PM
 */
public class TextPatchTest extends TestCase {
	private TextPatch tp;
	private TextPatch.Operation DELETE = TextPatch.Operation.DELETE;
	private TextPatch.Operation EQUAL = TextPatch.Operation.EQUAL;
	private TextPatch.Operation INSERT = TextPatch.Operation.INSERT;

	protected void setUp() {
		// Create an instance of the TextPatch object.
		tp = new TextPatch();
	}

	public void testDiffDelta() {
		String text1 = "The quick brown fox jumps over the lazy dog.";
		String text2 = "That quick brown fox jumped over a lazy dog.";
		LinkedList<Patch>  patches = tp.patch_make(text1, text2);

		//System.out.println("Patch :: " + dmp.patch_toText(patches));
		Object[] results =      tp.patch_apply(patches, text1);
		System.out.println("Applied :: " + results[0]);
	}



		// Private function for quickly building lists of diffs.
		private static LinkedList<Diff> diffList(Diff... diffs) {
			LinkedList<Diff> myDiffList = new LinkedList<Diff>();
			for (Diff myDiff : diffs) {
				myDiffList.add(myDiff);
			}
			return myDiffList;
		}
}
