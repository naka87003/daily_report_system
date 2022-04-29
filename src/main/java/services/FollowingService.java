package services;

import java.time.LocalDateTime;
import java.util.List;

import actions.views.EmployeeConverter;
import actions.views.EmployeeView;
import actions.views.FollowingConverter;
import actions.views.FollowingView;
import constants.JpaConst;
import models.Following;

/**
 * フォローテーブルの操作に関わる処理を行うクラス
 */
public class FollowingService extends ServiceBase {

    /**
     * フォローした際にデータを1件作成し、フォローテーブルに登録する
     * @param fv フォローデータ
     */
    public void create(FollowingView fv) {
        LocalDateTime ldt = LocalDateTime.now();
        fv.setCreatedAt(ldt);
        fv.setUpdatedAt(ldt);
        createInternal(fv);
    }

    /**
     * フォローデータを1件物理削除する
     * @param fv フォローデータ
     */
    public void destroy(FollowingView fv) {
        Following f = em.find(Following.class, fv.getId());

        em.getTransaction().begin();
        em.remove(f);
        em.getTransaction().commit();
    }

    /**
     * フォローデータを1件登録する
     * @param fv フォローデータ
     */
    private void createInternal(FollowingView fv) {

        em.getTransaction().begin();
        em.persist(FollowingConverter.toModel(fv));
        em.getTransaction().commit();

    }

    /**
     * 指定した従業員のフォローデータを、全件取得しFollowingViewのリストで返却する
     * @param employee 従業員
     * @return フォローデータのリスト
     */
    public List<FollowingView> getAllMine(EmployeeView ev) {

        List<Following> followings = em.createNamedQuery(JpaConst.Q_FOLLOW_GET_ALL_MINE, Following.class)
                .setParameter(JpaConst.JPQL_PARM_FOLLOWING_EMPLOYEE, EmployeeConverter.toModel(ev))
                .getResultList();
        return FollowingConverter.toViewList(followings);
    }

}