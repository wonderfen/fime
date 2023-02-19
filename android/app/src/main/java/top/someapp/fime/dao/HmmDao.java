package top.someapp.fime.dao;

import androidx.room.Dao;
import androidx.room.Query;
import top.someapp.fime.entity.HmmEmission;
import top.someapp.fime.entity.HmmStart;
import top.someapp.fime.entity.HmmStartAndEmission;
import top.someapp.fime.entity.HmmTransition;

import java.util.Collection;
import java.util.List;

/**
 * @author zwz
 * Create on 2023-02-01
 */
@Dao
public interface HmmDao {

    String START_TABLE = HmmStart.TABLE_NAME;
    String EMISSION_TABLE = HmmEmission.TABLE_NAME;
    String TRANSITION_TABLE = HmmTransition.TABLE_NAME;
    String START_AND_EMISSION_VIEW = HmmStartAndEmission.VIEW_NAME;

    @Query("select power_ from " + START_TABLE + " where text_ = :text limit 1")
    double start(String text);

    @Query("select power_ from " + EMISSION_TABLE + " where text_ = :text and code = :code order "
            + "by power_ desc limit 1")
    double emission(String text, String code);

    @Query("select * from " + EMISSION_TABLE + " where code like :prefix || '%' order "
            + "by power_ desc limit :limit")
    List<HmmEmission> codeStartWith(String prefix, int limit);

    @Query("select from_, to_, power_ from " + TRANSITION_TABLE
            + " where from_ || to_ in(:fromTo) order by power_ desc limit :limit")
    List<HmmTransition> transition(Collection<String> fromTo, int limit);

    @Query("select text_, code, start_, emission from " + START_AND_EMISSION_VIEW
            + " where code = :code order by emission desc, start_ desc limit :limit")
    List<HmmStartAndEmission> getStartAndEmission(String code, int limit);

    @Query("select text_, code, start_, emission from " + START_AND_EMISSION_VIEW
            + " where code like :code order by start_ desc, emission desc limit :limit")
    List<HmmStartAndEmission> getStartAndEmissionLike(String code, int limit);
}
