package jp.alhinc.calculate_sales;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CalculateSales {

	// 支店定義ファイル名
	private static final String FILE_NAME_BRANCH_LST = "branch.lst";

	// 支店別集計ファイル名
	private static final String FILE_NAME_BRANCH_OUT = "branch.out";

	// エラーメッセージ
	private static final String UNKNOWN_ERROR = "予期せぬエラーが発生しました";
	private static final String FILE_NOT_EXIST = "支店定義ファイルが存在しません";
	private static final String FILE_INVALID_FORMAT = "支店定義ファイルのフォーマットが不正です";

	/**
	 * メインメソッド
	 *
	 * @param コマンドライン引数
	 *
	 * @param args コマンドライン引数
	 */
	public static void main(String[] args) {
		// 支店コードと支店名を保持するMap
		Map<String, String> branchNames = new HashMap<>();
		// 支店コードと売上金額を保持するMap
		Map<String, Long> branchSales = new HashMap<>();

		// 支店定義ファイル読み込み処理
		if (!readFile(args[0], FILE_NAME_BRANCH_LST, branchNames, branchSales)) {
			return;
		}

		// ※ここから集計処理を作成してください。(処理内容2-1、2-2)
		//listFilesを使⽤してfilesという配列に、
		//指定したパスに存在する全てのファイル(または、ディレクトリ)の情報を格納します。
		File[] files = new File(args[0]).listFiles();

		// 先にファイルの情報を格納する List(ArrayList) を宣⾔します。
		List<File> rcdFiles = new ArrayList<>();

		//filesの数だけ繰り返すことで、
		//指定したパスに存在する全てのファイル(または、ディレクトリ)の数だけ繰り返されます。

		for (int i = 0; i < files.length; i++) {
			// ファイル名の取得
			String FileName = files[i].getName();
			//\\2つで次に指定する文字を文字列として扱う
			if (FileName.matches("^[0-9]{8}\\.rcd")) {
				// 売上ファイルの条件に当てはまったものだけ、List(ArrayList) に追加します。
				rcdFiles.add(files[i]);
			}
		}
		//rcdFilesに複数の売上ファイルの情報を格納しているので、その数だけ繰り返します。
		for (int i = 0; i < rcdFiles.size(); i++) {
			// 売上ファイルを1つずつ読み込む処理（readRcdの呼び出し等）を記述します
			// 処理対象のファイルをリストから1つ取り出す
			File targetFile = rcdFiles.get(i);
			// 作成した readRcd メソッドをここで呼び出す！
			if (!readRcd(args[0], targetFile.getName(), branchNames, branchSales)) {
				return; // 読み込み中にエラーが起きたらプログラムを終了する
			}
		}

		// メインメソッドの最後（すべての集計が終わった後）で書き込みを行います
		if (!writeFile(args[0], FILE_NAME_BRANCH_OUT, branchNames, branchSales)) {
			return;
			}
	}

	// 売上ファイル読み込み処理
	private static boolean readRcd(String path, String money, Map<String, String> branchNames,
			Map<String, Long> branchSales) {
		BufferedReader br = null;
		try {
			File file = new File(path, money);
			FileReader fr = new FileReader(file);
			br = new BufferedReader(fr);

			String branchCodeline;
			String fileSaleline;
			// 一行ずつ読み込む
			while ((branchCodeline = br.readLine()) != null && (fileSaleline = br.readLine())!= null) {

				// 分割したデータから「支店コード」と「売上金額」を抜き出す
				//支店コードを抽出
				String branchCode = branchCodeline;
				//売上額を抽出（数字に変換）
				long fileSale = Long.parseLong(fileSaleline);
				// 初期値を0にする
				long currentSale = 0;

				// すでにMapにその支店が存在する場合のみ、現在の売上を取り出す
				if (branchSales.containsKey(branchCode)) {
				    currentSale = branchSales.get(branchCode);
				}
				// 新しい売上を足し算する
				long totalSale = currentSale + fileSale;
				//計算が終わった合計金額（totalSale）をMapに保存する
				branchSales.put(branchCode, totalSale);
			}
			return true;

		} catch (IOException e) {
			System.out.println(UNKNOWN_ERROR);
			return false;
		} finally {
			// ファイルを開いている場合
			if (br != null) {
				try {
					// ファイルを閉じる
					br.close();
				} catch (IOException e) {
					System.out.println(UNKNOWN_ERROR);
					return false;
				}
			}
		}
	}

	/**
	 * 支店定義ファイル読み込み処理
	 *
	 * @param フォルダパス
	 * @param ファイル名
	 * @param 支店コードと支店名を保持するMap
	 * @param 支店コードと売上金額を保持するMap
	 * @return 読み込み可否
	 */

	private static boolean readFile(String path, String fileName, Map<String, String> branchNames,
			Map<String, Long> branchSales) {
		BufferedReader br = null;

		try {
			File file = new File(path, fileName);
			FileReader fr = new FileReader(file);
			br = new BufferedReader(fr);

			String line;
			while ((line = br.readLine()) != null) {
				// ※ここの読み込み処理を変更してください。(処理内容1-2)
				//split を使って「,」(カンマ)で分割すると、
				//items[0] には⽀店コード、items[1] には⽀店名が格納されます。
				String[] items = line.split(",");
				// 固定の文字ではなく、ファイルから読み込んだデータをMapに追加します
				String branchCode = items[0]; // 支店コード
				String branchName = items[1]; // 支店名
				//Mapに追加する2つの情報を putの引数として指定します。
				branchNames.put(branchCode, branchName);
				branchSales.put(branchCode, 0L); // 初期の売上金額は一律「0円」で登録
			}
		} catch (IOException e) {
			System.out.println(UNKNOWN_ERROR);
			return false;
		} finally {
			// ファイルを開いている場合
			if (br != null) {
				try {
					// ファイルを閉じる
					br.close();
				} catch (IOException e) {
					System.out.println(UNKNOWN_ERROR);
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * 支店別集計ファイル書き込み処理
	 *
	 * @param フォルダパス
	 * @param ファイル名
	 * @param 支店コードと支店名を保持するMap
	 * @param 支店コードと売上金額を保持するMap
	 * @return 書き込み可否
	 */
		private static boolean writeFile(String path, String fileName, Map<String, String> branchNames,
				Map<String, Long> branchSales) {
			// ※ここに書き込み処理を作成してください。(処理内容3-1)
			BufferedWriter bw = null;
			try {
				File file = new File(path, fileName);
				FileWriter fw = new FileWriter(file);
				bw = new BufferedWriter(fw);

				for (String key : branchNames.keySet()) {
					String name = branchNames.get(key);
					Long sales = branchSales.get(key);
					String line = key + "," + name + "," + sales;
					// ファイルへの書き込み処理
					bw.write(line); // 組み立てた文字列を書き込む
					bw.newLine();   // 次のループのために改行を入れる
				}
					//keyという変数には、Mapから取得したキーが代入されています。
					//拡張for⽂で繰り返されているので、1つ⽬のキーが取得できたら、
					//2つ⽬の取得...といったように、次々とkeyという変数に上書きされていきます。
			}catch(IOException e) {
				System.out.println(UNKNOWN_ERROR);
			}finally {
				try {
					if (bw != null) {
						bw.close();
					}
				} catch (IOException e) {
					e.printStackTrace();
					return false;
				}
			}
			return true;
		}
}

