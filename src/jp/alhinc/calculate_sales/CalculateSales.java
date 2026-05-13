package jp.alhinc.calculate_sales;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
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
		File[] files = new File("C:\\Users\\trainee1723\\Desktop\\売上集計課題").listFiles();

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
				for (int n = 0; n < rcdFiles.size(); n++) {
					// 売上ファイルを1つずつ読み込む処理（readRcdの呼び出し等）を記述します
					// 処理対象のファイルをリストから1つ取り出す
					File targetFile = rcdFiles.get(n);

					// 作成した readRcd メソッドをここで呼び出す！
					if (!readRcd(args[0], targetFile.getName(), branchNames, branchSales)) {
						return; // 読み込み中にエラーが起きたらプログラムを終了する
					}
				}

	// メインメソッドの最後（すべての集計が終わった後）で書き込みを行います
	//if (!writeFile(args[0], FILE_NAME_BRANCH_OUT, branchNames, branchSales)) {
	//return;
	//}
	}
	// 売上ファイル読み込み処理
	private static boolean readRcd(String path, String money, Map<String, String> branchNames,
			Map<String, Long> branchSales) {
		BufferedReader br = null;
		try {
			File file = new File(path, money);
			FileReader fr = new FileReader(file);
			br = new BufferedReader(fr);

			String line;
			// 一行ずつ読み込む
			while ((line = br.readLine()) != null) {
				//カンマで分割する
				String[] items = line.split(",");
				//もし正しく2つに分割できていなければ、この行の処理はスキップします
				if (items.length < 2) {
					continue;
				}
				// 分割したデータから「支店コード」と「売上金額」を抜き出す
				//支店コードを抽出
				String branchCode = items[0];
				//売上額を抽出（数字に変換）
				long fileSale = Long.parseLong(items[1]);
				// 現在Mapに入っている、その支店の「これまでの売上合計」を取り出す
				long currentSale = branchSales.get(branchCode);
				// 新しい売上を足し算する
				long totalSale = currentSale + fileSale;
				//計算が終わった合計金額（totalSale）をMapに保存する
				branchSales.put(branchCode, totalSale);

				System.out.println(line);
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

		//売上ファイルから読み込んだ売上金額をMapに加算していくために、型の変換を行います。
		for (String key : branchSales.keySet()) {
			if (branchSales.get(key) != null) {
				long cost = branchSales.get(key);
				branchSales.put(key, cost);
			}
		}

		return true;
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

				// 正しく2つに分割できていなければスキップ
				if (items.length < 2) {
					continue;
				}

				// 固定の文字ではなく、ファイルから読み込んだデータをMapに追加します
				String branchCode = items[0]; // 支店コード
				String branchName = items[1]; // 支店名

				//Mapに追加する2つの情報を putの引数として指定します。
				branchNames.put(branchCode, branchName);
				branchSales.put(branchCode, 0L); // 初期の売上金額は一律「0円」で登録

				System.out.println(line);
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
		private static boolean writeFile(String path, String fileNameBranchOut, Map<String, String> branchNames,
				Map<String, Long> branchSales) {
			// ※ここに書き込み処理を作成してください。(処理内容3-1)
			return false;
		}
}

