package jp.alhinc.calculate_sales;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
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

		// コマンドライン引数が1つ設定されているか確認します。
		if (args.length != 1) {
			//コマンドライン引数が1つ設定されていなかった場合は、
			//エラーメッセージをコンソールに表⽰します。
			System.out.println(UNKNOWN_ERROR);
			return;
		}

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
			if (FileName.matches(".*\\.rcd$")) {
				// 対象がファイルであり、「数字8桁.rcd」なのか判定
				if (files[i].isFile() && FileName.matches("^[0-9]{8}\\.rcd")) {
					// 売上ファイルの条件に当てはまったものだけ、List(ArrayList) に追加
					rcdFiles.add(files[i]);
				} else {
					// 条件に当てはまらないフォルダや、名前の形式が違うファイルがあった場合
					System.out.println(UNKNOWN_ERROR);
					return;
				}
			}
		}
		//連番チェックを⾏う前に、売上ファイルを保持しているListをソートする
		Collections.sort(rcdFiles);
		// ⽐較回数は売上ファイルの数よりも1回少ないため、
		// 繰り返し回数は売上ファイルのリストの数よりも1つ⼩さい数です。
		for (int i = 0; i < rcdFiles.size() - 1; i++) {

			// 現在のファイル名と次のファイル名を取得（Fileオブジェクトから名前を取り出す）
			String formerFileName = rcdFiles.get(i).getName();
			String latterFileName = rcdFiles.get(i + 1).getName();

			// ⽐較する2つのファイル名の先頭から数字の8⽂字を切り出し、int型に変換します。
			int former = Integer.parseInt(formerFileName.substring(0, 8));
			int latter = Integer.parseInt(latterFileName.substring(0, 8));

			// 2つのファイル名の数字を⽐較して、差が1ではなかったら、
			// エラーメッセージをコンソールに表⽰します。
			if ((latter - former) != 1) {
				System.out.println(FILE_INVALID_FORMAT);
				return;
			}
		}

		//rcdFilesに複数の売上ファイルの情報を格納しているので、その数だけ繰り返します。
		for (int i = 0; i < rcdFiles.size(); i++) {

			// 売上ファイルを1つずつ読み込む処理
			// 処理対象のファイルをリストから1つ取り出す
			File targetFile = rcdFiles.get(i);

			//読み込む対象ファイルオブジェクトを生成
			File file = new File(args[0], targetFile.getName());
			// BufferedReaderを使ってファイルを開く

			BufferedReader br = null;

			try {
				br = new BufferedReader(new FileReader(file));
				String branchCodeline;
				String fileSaleline;
				// コード保持用
				List<String> codeList = new ArrayList<>();
				// 売上額保持用
				List<Long> saleList = new ArrayList<>();
				// 行数チェック保持用
				List<String> fileLines = new ArrayList<>();
				try (BufferedReader checkBr = new BufferedReader(new FileReader(file))) {
					String checkLine;
					while ((checkLine = checkBr.readLine()) != null) {
						fileLines.add(checkLine);
					}
				}
				////売上ファイルの⾏数が2⾏ではなかった場合は、
				//エラーメッセージをコンソールに表⽰します。
				if (fileLines.size() != 2) {
					System.out.println(file.getName() + "のフォーマットが不正です");
					return;
				}

				// br.readLine()で1行目（支店コードと2行目（売上額)を同時に読み込みデータがなくなったらループが自動で終了
				while ((branchCodeline = br.readLine()) != null && (fileSaleline = br.readLine()) != null) {
					// 支店コード保持用
					String branchCode = branchCodeline;

					// 売上⾦額が数字ではなかった場合は、
					// エラーメッセージをコンソールに表⽰します。
					// 「^[0-9]+$」は半角数字が1文字以上連続していなかったら
					if (!fileSaleline.matches("^[0-9]+$")) {
						System.out.println(UNKNOWN_ERROR);
						return;
					}
					//数字（long型)に変換
					long fileSale = Long.parseLong(fileSaleline);

					//リストへの情報保持
					codeList.add(branchCode);
					saleList.add(fileSale);

					if (!branchNames.containsKey(branchCode)) {
						System.out.println(file.getName() + "の支店コードが不正です");
						return;
					}

					//Mapから値を取り出す
					Long savedSale = branchSales.get(branchCode);
					// 初期化
					long currentSale = 0;
					// savedSale が null ではない時その取れた過去の売上額を currentSale に上書き
					if (savedSale != null) {
						currentSale = savedSale;
					}
					//合計金額の計算
					long totalSale = currentSale + fileSale;
					if (totalSale >= 10000000000L) {
						// 売上⾦額が11桁以上の場合、エラーメッセージをコンソールに表⽰します。
						System.out.println("合計金額が10桁を超えました");
						return;
					}

					//Mapへの保存
					branchSales.put(branchCode, totalSale);
				}

			} catch (IOException e) {
				// 読み込み中にエラーが起きたらエラーメッセージを出してプログラムを終了する
				System.out.println(UNKNOWN_ERROR);
				return;
			} finally {
				// ファイルを開いている場合
				if (br != null) {
					try {
						// ファイルを閉じる
						br.close();
					} catch (IOException e) {
						System.out.println(UNKNOWN_ERROR);
						return;
					}
				}
			}
		}

		// メインメソッドの最後（すべての集計が終わった後）で書き込みを行います
		if (!writeFile(args[0], FILE_NAME_BRANCH_OUT, branchNames, branchSales)) {
			return;
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
			//ファイルの存在チェック
			if (!file.exists()) {
				System.out.println(FILE_NOT_EXIST);
				return false;
			}
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

				//⽀店定義ファイルの仕様が満たされていない場合、 エラーメッセージをコンソールに表⽰します。
				//1行に⽀店コードと⽀店名が「,」(カンマ)で区切られて記載されていること、⽀店コードは数字3桁であること
				if ((items.length != 2) || (!branchCode.matches("^[0-9]{3}$"))) {
					System.out.println(FILE_INVALID_FORMAT); // ※指定のエラーメッセージ定数名に変えてください
					return false;
				}

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
				bw.newLine(); // 次のループのために改行を入れる
			}
			//keyという変数には、Mapから取得したキーが代入されています。
			//拡張for⽂で繰り返されているので、1つ⽬のキーが取得できたら、
			//2つ⽬の取得...といったように、次々とkeyという変数に上書きされていきます。
		} catch (IOException e) {
			System.out.println(UNKNOWN_ERROR);
		} finally {
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
