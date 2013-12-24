//import java.io.IOException;
//import java.io.InputStream;
//import java.io.OutputStream;
//import java.util.PriorityQueue;
//
//public class SimpleHuffProcessor implements IHuffProcessor {
//
//	private HuffViewer myViewer;
//	private int[] frequency = new int[257];
//	private PriorityQueue<TreeNode> pq = new PriorityQueue<TreeNode>();
//	private String[] myCodes = new String[257];
//	private int tempTracker = 0;
//
//	public int compress(InputStream in, OutputStream out, boolean force) throws IOException {
//
//		// force-compression
//		if(tempTracker < 0 & !force) {
//			myViewer.showError("Compression uses" +" " + (-tempTracker) + " " + "more bits \n use force-compression to compress ");		
//		}
//
//		BitOutputStream bos = new BitOutputStream(out);
//		int  bitsWritten = 0;
//		// write out the magic number 
//		bos.writeBits(BITS_PER_INT, MAGIC_NUMBER);
//		bitsWritten += 32;
//
//		//write out the header
//		for(int k=0; k < ALPH_SIZE; k++){
//			bos.writeBits(BITS_PER_INT, frequency[k]);
//			bitsWritten += 32;
//		}
//
//		Integer inbits;
//		BitInputStream bis = new BitInputStream(in);
//
//
//		// writing out the compressed text one charcter at a time
//		while ((inbits = bis.readBits(BITS_PER_WORD)) != -1) {
//			String lace = myCodes[inbits];
//
//			for(int i = 0; i < lace.length(); i++){
//				bos.writeBits(1, lace.charAt(i) == '0'? 0 : 1);
//				bitsWritten += 1;
//			}
//
//		} 
//		// writing out the pseudo eof
//		String lace = myCodes[PSEUDO_EOF];
//		for(int i = 0; i < lace.length(); i++){
//			bos.writeBits(1, lace.charAt(i) == '0'? 0 : 1);
//			bitsWritten += 1;
//		}
//		bis.close();
//		bos.close();
//
//		return bitsWritten;
//	}
//
//	public int preprocessCompress(InputStream in) throws IOException {
//
//		int inbits;
//		BitInputStream bis = new BitInputStream(in);
//		int tracker = 0;
//
//		frequency = new int[257];
//
//		while ((inbits = bis.readBits(BITS_PER_WORD)) != -1) {
//			for(int i = 0; i < 256; i++){
//				if(inbits == i ){
//					frequency[i] += 1;
//					tracker += BITS_PER_WORD; // tracks the total number of bits in original file
//				}
//			} 
//		}
//		bis.close();
//
//		frequency[256] = 1;	// the eof count
//
//		//populating the priority queue
//		TreeNode myRoot = buildTree();
//		createHuffCodings(myRoot, "");
//		int trackCompressed = 0;
//		// counting the number of bits in the compressed file from the 
//		//counts of chunks and their lengths
//		for(int i = 0; i < 256; i++){
//			if(myCodes[i] != null){
//				trackCompressed += (frequency[i] * myCodes[i].length());	
//			}
//		}
//		int headerSize = (BITS_PER_INT * ALPH_SIZE);//calculating length of the header
//		headerSize += 32; // taking into account the magic number
//		trackCompressed += headerSize;
//		int finalTracker = tracker- trackCompressed;
//
//		return (tempTracker = finalTracker);
//	}
//
//	public void setViewer(HuffViewer viewer) {
//		myViewer = viewer;
//	}
//
//	public int uncompress(InputStream in, OutputStream out) throws IOException {
//
//		BitInputStream bis = new BitInputStream(in);
//		BitOutputStream bos = new BitOutputStream(out);
//		int uncompressedSize = 0;
//
//		int magic = bis.readBits(BITS_PER_INT); 
//		if (magic != MAGIC_NUMBER){ 
//			throw new IOException("magic number not right"); 
//		}
//
//		for(int k=0; k < ALPH_SIZE; k++){
//			int bits = bis.readBits(BITS_PER_INT);
//			frequency[k] = bits;
//
//		}
//
//		// rebuild the Huffman tree
//		buildTree();
//
//		TreeNode myRoot = buildTree();
//		TreeNode tnode = myRoot;
//
//		while (true) {
//			int bits = bis.readBits(1);      
//			if (bits == -1) {
//				throw new IOException("error reading bits, no PSEUDO-EOF");
//}
//			// use the zero/one value of the bit read to traverse Huffman coding tree 
//			if ( (bits & 1) == 0) {
//				tnode = tnode.myLeft;
//			} 
//			else {
//				tnode = tnode.myRight;
//			}
//
//			if (isLeaf(tnode)) {
//				
//				if (tnode.myValue == PSEUDO_EOF) break;
//				
//				else{
//					bos.writeBits(BITS_PER_WORD, tnode.myValue);
//					 uncompressedSize += BITS_PER_WORD;
//				}
//				tnode = myRoot;  // start back at top
//			}
//
//		}
//
//		bis.close();
//		bos.close();
//		
//		return uncompressedSize;
//	}
//
//	private void showString(String s){
//		myViewer.update(s);
//	}
//
//	// method to check if a node is a leaf
//	private  boolean isLeaf(TreeNode node) {
//		if(node == null) return false;
//		if(node.myValue != -1)
//			return true;
//
//		return false;
//	}
//
//
//	// method to traverse tree and track the huffman path from the tree
//	private  void createHuffCodings (TreeNode root, String path) {
//
//		if (!isLeaf(root)) {
//			createHuffCodings(root.myLeft,  path + '0');
//			createHuffCodings(root.myRight, path + '1');
//		}
//		else { 
//			myCodes[root.myValue] = path;
//		}
//	}
//
//	// method to build the huffman tree
//	public TreeNode buildTree(){
//		pq.clear();
//		for(int i = 0; i < 256; i++){ 
//			if(frequency[i] > 0){
//				TreeNode node = new TreeNode(i, frequency[i], null, null);
//				pq.add(node);
//			}
//		}
//		// creating a node of the Pseudo eof and adding it to the priority queue
//		TreeNode eof = new TreeNode(PSEUDO_EOF, 1, null, null);	 
//		pq.add(eof);
//
//		while(pq.size() > 1){
//			TreeNode left = pq.remove();
//			TreeNode right = pq.remove();
//			int weight = left.myWeight + right.myWeight;
//			TreeNode tempRoot = new TreeNode(-1, weight, left, right);
//			pq.add(tempRoot);
//		}
//		TreeNode myRoot = pq.remove();
//		return myRoot;
//	}
//}
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

public class SimpleHuffProcessor implements IHuffProcessor {

	private HuffViewer myViewer;
	private static Map<Integer, String> map = new HashMap<Integer, String>();
	private static int[] bitCodes = new int[256];
	TreeNode root;
	private static TreeNode EOFNode = new TreeNode(IHuffConstants.PSEUDO_EOF, 1);
	private static PriorityQueue<TreeNode> q = new PriorityQueue<TreeNode>();
	private static String header = "";
	private static int bitsRead;
	int bitsWritten;
	private int compressedSize;

	public void setViewer(HuffViewer viewer) {
		myViewer = viewer;
	}

	private void showString(String s) {
		myViewer.update(s);
	}

	void makeQueue(int[] bitCodes) {
		q.clear();
		for (int k = 0; k < 256; k++) {
			if (bitCodes[k] == 0)
				continue;
			q.add(new TreeNode(k, bitCodes[k]));
		}
		// add our Pseudo-EOF to our Priority queue
		q.add(EOFNode);

	}

	private boolean isLeaf(TreeNode node) {
		if (node == null)
			return false;
		if (node.myValue != -1)
			return true;
		return false;
	}

	void createCodings(TreeNode node, String s) {
		if (isLeaf(node)) { // we found a leaf
			map.put(node.myValue, s);
		} else {
			createCodings(node.myLeft, s + "0");
			createCodings(node.myRight, s + "1");
		}
	}

	TreeNode makeTree(PriorityQueue<TreeNode> q) {
		TreeNode root = null;
		while (q.size() > 1) {
			TreeNode left = q.remove();
			TreeNode right = q.remove();
			int total = left.myWeight + right.myWeight;
			TreeNode jingleBell = new TreeNode(-1, total, left, right);
			q.add(jingleBell);
			root = jingleBell;
		}
		return root;
	}

	public int[] countLetters(InputStream in) throws IOException {
		BitInputStream inStream = new BitInputStream(in);
		bitsRead = 0;
		int[] result = new int[ALPH_SIZE];
		int current = inStream.readBits(BITS_PER_WORD);

		while (current != -1) {
			bitsRead += 8;
			result[current]++;
			current = inStream.readBits(BITS_PER_WORD);
		}
		return result;
	}

	/**
	 * Preprocess data so that compression is possible --- count
	 * characters/create tree/store state so that a subsequent call to compress
	 * will work. The InputStream is <em>not</em> a BitInputStream, so wrap it
	 * int one as needed.
	 * 
	 * @param in
	 *            is the stream which could be subsequently compressed
	 * @return number of bits saved by compression or some other measure
	 */

	public int preprocessCompress(InputStream in) throws IOException {
		BitInputStream input = new BitInputStream(in);

		bitCodes = countLetters(input);
		makeQueue(bitCodes);
		root = makeTree(q);
		createCodings(root, "");
		
		input.close();

		//calculate compressed file size
		compressedSize = 0;
		compressedSize += 32 + 256 * 32;
		for (int k = 0; k < 256; k++) {
			if (bitCodes[k] == 0)
				continue;
			compressedSize += bitCodes[k] * ((map.get(k)).length());
		}

		return bitsRead-compressedSize;
	}

	/**
	 * Compresses input to output, where the same InputStream has previously
	 * been pre-processed via <code>preprocessCompress</code> storing state used
	 * by this call.
	 * 
	 * @param in
	 *            is the stream being compressed (not a BitInputStream)
	 * @param out
	 *            is bound to a file/stream to which bits are written for the
	 *            compressed file (not a BitOutputStream)
	 * @return the number of bits written
	 */

	public int compress(InputStream in, OutputStream out, boolean force)
			throws IOException {

		BitInputStream input = new BitInputStream(in);
		BitOutputStream output = new BitOutputStream(out);

		// force compression
		if (!force && compressedSize > bitsRead) {
			int difference = compressedSize - bitsRead;
			myViewer.showMessage("No compression");
			myViewer.showError(("Compression uses " + difference
					+ " more bits\nUse force compress to compress"));
		}

		output.writeBits(BITS_PER_INT, MAGIC_NUMBER);

		// write out frequencies in the header
		for (int k = 0; k < ALPH_SIZE; k++) {
			output.writeBits(BITS_PER_INT, bitCodes[k]);
		}

		////Tree-Header Extra Credit

		for (int k = 0; k < header.length(); k++) {
			output.writeBits(1, Integer.parseInt(header.substring(k, k + 1)));
		}

		int current = input.readBits(BITS_PER_WORD);
		
		while (current != -1) {
			String write = map.get(current);
			for (int k = 0; k < write.length(); k++) {
				output.writeBits(1,
						Integer.parseInt(write.substring(k, k + 1)));
				compressedSize++;
			}
			current = input.readBits(BITS_PER_WORD);
		}

		String EOF = map.get(256);
		for (int k = 0; k < EOF.length(); k++) {
			output.writeBits(1, Integer.parseInt(EOF.substring(k, k + 1)));
			compressedSize++;
		}

		output.flush();
		input.close();
		output.close();
		
		
		return compressedSize;
	}

	/**
	 * Uncompress a previously compressed stream in, writing the uncompressed
	 * bits/data to out.
	 * 
	 * @param in
	 *            is the previously compressed data (not a BitInputStream)
	 * @param out
	 *            is the uncompressed file/stream
	 * @return the number of bits written to the uncompressed file/stream
	 */

	public int uncompress(InputStream in, OutputStream out) throws IOException {

		BitInputStream inStream = new BitInputStream(in);
		BitOutputStream outStream = new BitOutputStream(out);

		int unCompressedSize = 0;

		int magic = inStream.readBits(BITS_PER_INT);
		if (magic != MAGIC_NUMBER) {
			throw new IOException("Magic number not right.");
		}

		int[] myCounts = new int[256];
		for (int k = 0; k < ALPH_SIZE; k++) {
			int bits = inStream.readBits(BITS_PER_INT);
			myCounts[k] = bits;
		}

		//rebuild Huffman tree

		makeQueue(myCounts);
		TreeNode root = makeTree(q);
		createCodings(root, "");

		int bits;
		TreeNode current = root;
		while (true) {
			bits = inStream.readBits(1);

			if ((bits & 1) == 1) {
				current = current.myRight;
			} else {
				current = current.myLeft;
			}

			if (isLeaf(current)) {
				if (current.myValue == PSEUDO_EOF) {
					break;
				} else {
					outStream.writeBits(BITS_PER_WORD, current.myValue);
					unCompressedSize += BITS_PER_WORD;
					current = root;
				}
			}
		}

		outStream.flush();
		outStream.close();
		inStream.close();
		
		myViewer.showMessage("Uncompressed file size:" + " " + unCompressedSize + "bits "  );
		return unCompressedSize;
	}
}
