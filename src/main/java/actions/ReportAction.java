package actions;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import javax.servlet.ServletException;

import actions.views.EmployeeView;
import actions.views.FollowingView;
import actions.views.LikeView;
import actions.views.ReportView;
import constants.AttributeConst;
import constants.ForwardConst;
import constants.JpaConst;
import constants.MessageConst;
import services.FollowingService;
import services.LikeService;
import services.ReportService;

/**
 * 日報に関する処理を行うActionクラス
 *
 */
public class ReportAction extends ActionBase {

    private ReportService service;
    private LikeService likeService;
    private FollowingService followingService;

    /**
     * メソッドを実行する
     */
    @Override
    public void process() throws ServletException, IOException {

        service = new ReportService();
        likeService = new LikeService();
        followingService = new FollowingService();

        //メソッドを実行
        invoke();
        service.close();
    }

    /**
     * 一覧画面を表示する
     * @throws ServletException
     * @throws IOException
     */
    public void index() throws ServletException, IOException {

        //指定されたページ数の一覧画面に表示する日報データを取得
        int page = getPage();
        List<ReportView> reports = service.getAllPerPage(page);

        //全日報データの件数を取得
        long reportsCount = service.countAll();

        putRequestScope(AttributeConst.REPORTS, reports); //取得した日報データ
        putRequestScope(AttributeConst.REP_COUNT, reportsCount); //全ての日報データの件数
        putRequestScope(AttributeConst.PAGE, page); //ページ数
        putRequestScope(AttributeConst.MAX_ROW, JpaConst.ROW_PER_PAGE); //1ページに表示するレコードの数

        //セッションにフラッシュメッセージが設定されている場合はリクエストスコープに移し替え、セッションからは削除する
        String flush = getSessionScope(AttributeConst.FLUSH);
        if (flush != null) {
            putRequestScope(AttributeConst.FLUSH, flush);
            removeSessionScope(AttributeConst.FLUSH);
        }

        //一覧画面を表示
        forward(ForwardConst.FW_REP_INDEX);
    }

    /**
     * 新規登録画面を表示する
     * @throws ServletException
     * @throws IOException
     */
    public void entryNew() throws ServletException, IOException {

        putRequestScope(AttributeConst.TOKEN, getTokenId()); //CSRF対策用トークン

        //日報情報の空インスタンスに、日報の日付＝今日の日付を設定する
        ReportView rv = new ReportView();
        rv.setReportDate(LocalDate.now());
        putRequestScope(AttributeConst.REPORT, rv); //日付のみ設定済みの日報インスタンス

        //新規登録画面を表示
        forward(ForwardConst.FW_REP_NEW);

    }

    /**
     * 新規登録を行う
     * @throws ServletException
     * @throws IOException
     */
    public void create() throws ServletException, IOException {

        //CSRF対策 tokenのチェック
        if (checkToken()) {

            //日報の日付が入力されていなければ、今日の日付を設定
            LocalDate day = null;
            if (getRequestParam(AttributeConst.REP_DATE) == null
                    || getRequestParam(AttributeConst.REP_DATE).equals("")) {
                day = LocalDate.now();
            } else {
                day = LocalDate.parse(getRequestParam(AttributeConst.REP_DATE));
            }

            //セッションからログイン中の従業員情報を取得
            EmployeeView ev = (EmployeeView) getSessionScope(AttributeConst.LOGIN_EMP);

            //パラメータの値をもとに日報情報のインスタンスを作成する
            ReportView rv = new ReportView(
                    null,
                    ev, //ログインしている従業員を、日報作成者として登録する
                    day,
                    getRequestParam(AttributeConst.REP_TITLE),
                    getRequestParam(AttributeConst.REP_CONTENT),
                    null,
                    null,
                    0);

            //日報情報登録
            List<String> errors = service.create(rv);

            if (errors.size() > 0) {
                //登録中にエラーがあった場合

                putRequestScope(AttributeConst.TOKEN, getTokenId()); //CSRF対策用トークン
                putRequestScope(AttributeConst.REPORT, rv);//入力された日報情報
                putRequestScope(AttributeConst.ERR, errors);//エラーのリスト

                //新規登録画面を再表示
                forward(ForwardConst.FW_REP_NEW);

            } else {
                //登録中にエラーがなかった場合

                //セッションに登録完了のフラッシュメッセージを設定
                putSessionScope(AttributeConst.FLUSH, MessageConst.I_REGISTERED.getMessage());

                //一覧画面にリダイレクト
                redirect(ForwardConst.ACT_REP, ForwardConst.CMD_INDEX);
            }
        }
    }

    /**
     * 詳細画面を表示する
     * @throws ServletException
     * @throws IOException
     */
    public void show() throws ServletException, IOException {

        //idを条件に日報データを取得する
        ReportView rv = service.findOne(toNumber(getRequestParam(AttributeConst.REP_ID)));

        //セッションからログイン中の従業員情報を取得
        EmployeeView ev = (EmployeeView) getSessionScope(AttributeConst.LOGIN_EMP);

        //ログイン中の従業員が既にいいねをしたかどうかの変数
        boolean alreadyLiked = false;

        //ログイン中の従業員が既に日報の作成者をフォローしたかの確認
        boolean alreadyFollowed = false;

        if (rv == null) {
            //該当の日報データが存在しない場合はエラー画面を表示
            forward(ForwardConst.FW_ERR_UNKNOWN);

        } else {
            //ログイン中の従業員が既にいいねをしたかの確認
            List<LikeView> likes = likeService.getAllMine(rv);
            for (LikeView lv : likes) {
                if (lv.getEmployee().getId() == ev.getId()) {
                    alreadyLiked = true;
                    break;
                }
            }

            //ログイン中の従業員が既にフォローしているかの確認
            List<FollowingView> followings = followingService.getAllMine(ev);
            for (FollowingView fv : followings) {
                if (fv.getFollowedEmployee().getId() == rv.getEmployee().getId()) {
                    alreadyFollowed = true;
                    break;
                }
            }

            putRequestScope(AttributeConst.REPORT, rv); //取得した日報データ
            putRequestScope(AttributeConst.REP_ALREADY_LIKED, alreadyLiked); //既にいいねをしたかどうか
            putRequestScope(AttributeConst.REP_ALREADY_FOLLOWED, alreadyFollowed); //既にフォローをしたかどうか

            //詳細画面を表示
            forward(ForwardConst.FW_REP_SHOW);
        }
    }

    /**
     * 編集画面を表示する
     * @throws ServletException
     * @throws IOException
     */
    public void edit() throws ServletException, IOException {

        //idを条件に日報データを取得する
        ReportView rv = service.findOne(toNumber(getRequestParam(AttributeConst.REP_ID)));

        //セッションからログイン中の従業員情報を取得
        EmployeeView ev = (EmployeeView) getSessionScope(AttributeConst.LOGIN_EMP);

        if (rv == null || ev.getId() != rv.getEmployee().getId()) {
            //該当の日報データが存在しない、または
            //ログインしている従業員が日報の作成者でない場合はエラー画面を表示
            forward(ForwardConst.FW_ERR_UNKNOWN);

        } else {

            putRequestScope(AttributeConst.TOKEN, getTokenId()); //CSRF対策用トークン
            putRequestScope(AttributeConst.REPORT, rv); //取得した日報データ

            //編集画面を表示
            forward(ForwardConst.FW_REP_EDIT);
        }

    }

    /**
     * 更新を行う
     * @throws ServletException
     * @throws IOException
     */
    public void update() throws ServletException, IOException {

        //CSRF対策 tokenのチェック
        if (checkToken()) {

            //idを条件に日報データを取得する
            ReportView rv = service.findOne(toNumber(getRequestParam(AttributeConst.REP_ID)));

            //入力された日報内容を設定する
            rv.setReportDate(toLocalDate(getRequestParam(AttributeConst.REP_DATE)));
            rv.setTitle(getRequestParam(AttributeConst.REP_TITLE));
            rv.setContent(getRequestParam(AttributeConst.REP_CONTENT));

            //日報データを更新する
            List<String> errors = service.update(rv);

            if (errors.size() > 0) {
                //更新中にエラーが発生した場合

                putRequestScope(AttributeConst.TOKEN, getTokenId()); //CSRF対策用トークン
                putRequestScope(AttributeConst.REPORT, rv); //入力された日報情報
                putRequestScope(AttributeConst.ERR, errors); //エラーのリスト

                //編集画面を再表示
                forward(ForwardConst.FW_REP_EDIT);
            } else {
                //更新中にエラーがなかった場合

                //セッションに更新完了のフラッシュメッセージを設定
                putSessionScope(AttributeConst.FLUSH, MessageConst.I_UPDATED.getMessage());

                //一覧画面にリダイレクト
                redirect(ForwardConst.ACT_REP, ForwardConst.CMD_INDEX);

            }
        }
    }

    /**
     * いいねする
     * @throws ServletException
     * @throws IOException
     */
    public void like() throws ServletException, IOException {

        //idを条件に日報データを取得する
        ReportView rv = service.findOne(toNumber(getRequestParam(AttributeConst.REP_ID)));

        //いいね数を１加算し、設定する
        rv.setLikeCount(rv.getLikeCount() + 1);

        //日報データを更新する
        service.update(rv);

        //セッションからログイン中の従業員情報を取得
        EmployeeView ev = (EmployeeView) getSessionScope(AttributeConst.LOGIN_EMP);

        //いいね情報のインスタンスを作成する
        LikeView lv = new LikeView(
                null,
                rv,
                ev, //ログインしている従業員を、日報作成者として登録する
                null,
                null);

        //いいね情報をテーブルに登録する
        likeService.create(lv);

        //セッションに更新完了のフラッシュメッセージを設定
        putSessionScope(AttributeConst.FLUSH, MessageConst.I_LIKED.getMessage());

        //一覧画面にリダイレクト
        redirect(ForwardConst.ACT_REP, ForwardConst.CMD_INDEX);

    }

    /**
     * 詳細画面を表示する
     * @throws ServletException
     * @throws IOException
     */
    public void showLikes() throws ServletException, IOException {

        //idを条件に日報データを取得する
        ReportView rv = service.findOne(toNumber(getRequestParam(AttributeConst.REP_ID)));

        //指定されたページ数の一覧画面に表示する日報データを取得
        int page = getPage();
        List<LikeView> likes = likeService.getMinePerPage(rv, page);

        //指定された日報のいいねデータの件数を取得
        long likesCount = likeService.countAllMine(rv);

        putRequestScope(AttributeConst.REPORT, rv); //取得した日報データ
        putRequestScope(AttributeConst.REP_LIKES, likes); //取得したいいねデータ
        putRequestScope(AttributeConst.REP_LIKES_COUNT, likesCount); //指定された日報のいいねデータの件数

        putRequestScope(AttributeConst.PAGE, page); //ページ数
        putRequestScope(AttributeConst.MAX_ROW, JpaConst.ROW_PER_PAGE); //1ページに表示するレコードの数

        //一覧画面を表示
        forward(ForwardConst.FW_REP_SHOWLIKES);
    }

    /**
     * 従業員をフォローする
     * @throws ServletException
     * @throws IOException
     */
    public void follow() throws ServletException, IOException {

        //idを条件に日報データを取得する
        ReportView rv = service.findOne(toNumber(getRequestParam(AttributeConst.REP_ID)));

        //セッションからログイン中の従業員情報を取得
        EmployeeView ev = (EmployeeView) getSessionScope(AttributeConst.LOGIN_EMP);

        //フォロー情報のインスタンスを作成する
        FollowingView fv = new FollowingView(
                null,
                ev, //ログインしている従業員を、フォローした従業員として登録する
                rv.getEmployee(),
                null,
                null);

        //フォロー情報をテーブルに登録する
        followingService.create(fv);

        //セッションに更新完了のフラッシュメッセージを設定
        putSessionScope(AttributeConst.FLUSH, MessageConst.I_FOLLOWED.getMessage());

        //一覧画面にリダイレクト
        redirect(ForwardConst.ACT_REP, ForwardConst.CMD_SHOWTIMELINE);
    }

    /**
     * 従業員をフォローから外す
     * @throws ServletException
     * @throws IOException
     */
    public void unfollow() throws ServletException, IOException {

        //idを条件に日報データを取得する
        ReportView rv = service.findOne(toNumber(getRequestParam(AttributeConst.REP_ID)));

        //セッションからログイン中の従業員情報を取得
        EmployeeView ev = (EmployeeView) getSessionScope(AttributeConst.LOGIN_EMP);

        //ログイン中の従業員のフォローデータを削除
        List<FollowingView> followings = followingService.getAllMine(ev);
        for (FollowingView following : followings) {
            if (following.getFollowedEmployee().getId() == rv.getEmployee().getId()) {
                followingService.destroy(following);
            }
        }

        //セッションに更新完了のフラッシュメッセージを設定
        putSessionScope(AttributeConst.FLUSH, MessageConst.I_UNFOLLOWED.getMessage());

        //一覧画面にリダイレクト
        redirect(ForwardConst.ACT_REP, ForwardConst.CMD_SHOWTIMELINE);

    }

    /**
     * タイムラインを表示する
     * @throws ServletException
     * @throws IOException
     */

    public void showTimeline() throws ServletException, IOException {

        //セッションからログイン中の従業員情報を取得
        EmployeeView loginEmployee = (EmployeeView) getSessionScope(AttributeConst.LOGIN_EMP);

        //フォローしている従業員が作成した日報データを、指定されたページ数の一覧画面に表示する分取得する
        int page = getPage();
        List<ReportView> reports = service.getReportForTimelinePerPage(loginEmployee, page);

        //フォローしている従業員が作成した日報データの件数を取得
        long myReportsCount = service.countAllForTimeline(loginEmployee);

        putRequestScope(AttributeConst.REPORTS, reports); //取得した日報データ
        putRequestScope(AttributeConst.REP_COUNT, myReportsCount); //フォローしている従業員が作成した日報の数
        putRequestScope(AttributeConst.PAGE, page); //ページ数
        putRequestScope(AttributeConst.MAX_ROW, JpaConst.ROW_PER_PAGE); //1ページに表示するレコードの数

        //セッションにフラッシュメッセージが設定されている場合はリクエストスコープに移し替え、セッションからは削除する
        String flush = getSessionScope(AttributeConst.FLUSH);
        if (flush != null) {
            putRequestScope(AttributeConst.FLUSH, flush);
            removeSessionScope(AttributeConst.FLUSH);
        }

        //タイムラインを表示
        forward(ForwardConst.FW_REP_SHOWTIMELINE);
    }
}