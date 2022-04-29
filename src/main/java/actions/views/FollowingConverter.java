package actions.views;

import java.util.ArrayList;
import java.util.List;

import models.Following;

/**
 * フォローデータのDTOモデル⇔Viewモデルの変換を行うクラス
 *
 */
public class FollowingConverter {

    /**
     * ViewモデルのインスタンスからDTOモデルのインスタンスを作成する
     * @param fv FollowingViewのインスタンス
     * @return Followingのインスタンス
     */
    public static Following toModel(FollowingView fv) {
        return new Following(
                fv.getId(),
                EmployeeConverter.toModel(fv.getFollowingEmployee()),
                EmployeeConverter.toModel(fv.getFollowedEmployee()),
                fv.getCreatedAt(),
                fv.getUpdatedAt());
    }

    /**
     * DTOモデルのインスタンスからViewモデルのインスタンスを作成する
     * @param f Followingのインスタンス
     * @return FollowingViewのインスタンス
     */
    public static FollowingView toView(Following f) {

        if (f == null) {
            return null;
        }

        return new FollowingView(
                f.getId(),
                EmployeeConverter.toView(f.getFollowingEmployee()),
                EmployeeConverter.toView(f.getFollowedEmployee()),
                f.getCreatedAt(),
                f.getUpdatedAt());
    }

    /**
     * DTOモデルのリストからViewモデルのリストを作成する
     * @param list DTOモデルのリスト
     * @return Viewモデルのリスト
     */
    public static List<FollowingView> toViewList(List<Following> list) {
        List<FollowingView> fvs = new ArrayList<>();

        for (Following f : list) {
            fvs.add(toView(f));
        }

        return fvs;
    }

    /**
     * Viewモデルの全フィールドの内容をDTOモデルのフィールドにコピーする
     * @param r DTOモデル(コピー先)
     * @param rv Viewモデル(コピー元)
     */
    public static void copyViewToModel(Following f, FollowingView fv) {
        f.setId(fv.getId());
        f.setFollowingEmployee(EmployeeConverter.toModel(fv.getFollowingEmployee()));
        f.setFollowedEmployee(EmployeeConverter.toModel(fv.getFollowedEmployee()));
        f.setCreatedAt(fv.getCreatedAt());
        f.setUpdatedAt(fv.getUpdatedAt());

    }

}