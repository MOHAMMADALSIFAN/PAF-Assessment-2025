package vttp.batch5.paf.movies.repositories;

import java.sql.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import jakarta.json.JsonObject;
import vttp.batch5.paf.movies.models.DirectorStats;

@Repository
public class MySQLMovieRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private MongoMovieRepository mongoRepo;

    private static final String SQL_INSERT_MOVIE = """
            INSERT INTO imdb (imdb_id, vote_average, vote_count, release_date, 
                            revenue, budget, runtime)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;

    private Set<String> processedIds = new HashSet<>();

    // TODO: Task 2.3
    // You can add any number of parameters and return any type from the method
    @Transactional
    public void batchInsertMovies(List<JsonObject> movies) {
        try {
            List<JsonObject> uniqueMovies = movies.stream()
                    .filter(movie -> {
                        String imdbId = getStringValue(movie, "imdb_id");
                        if (processedIds.contains(imdbId)) {
                            return false;
                        }
                        processedIds.add(imdbId);
                        return true;
                    })
                    .collect(Collectors.toList());
            if (uniqueMovies.isEmpty()) {
                return;
            }
            List<Object[]> batchParams = uniqueMovies.stream()
                    .map(this::extractMovieParams)
                    .collect(Collectors.toList());
            jdbcTemplate.batchUpdate(SQL_INSERT_MOVIE, batchParams);
            mongoRepo.batchInsertMovies(uniqueMovies);
        } catch (Exception ex) {
            List<String> failedIds = movies.stream()
                    .map(m -> getStringValue(m, "imdb_id"))
                    .collect(Collectors.toList());
            mongoRepo.logError(failedIds, ex);
            throw ex;
        }
    }
    // TODO: Task 3
/*
    SELECT 
    IFNULL(SUM(revenue), 0) as total_revenue,
    IFNULL(SUM(budget), 0) as total_budget
    FROM imdb
    WHERE imdb_id IN ('<movie_ids>')
     */
    public DirectorStats getDirectorFinancials(List<String> movieIds) {
        if (movieIds.isEmpty()) {
            return new DirectorStats(null, 0, 0.0, 0.0, 0.0);
        }
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("SELECT IFNULL(SUM(revenue), 0) as total_revenue, ");
        sqlBuilder.append("IFNULL(SUM(budget), 0) as total_budget ");
        sqlBuilder.append("FROM imdb WHERE imdb_id IN (");
        for (int i = 0; i < movieIds.size(); i++) {
            if (i > 0) {
                sqlBuilder.append(",");
            }
            sqlBuilder.append("'").append(movieIds.get(i)).append("'");
        }
        sqlBuilder.append(")");
        try {
            Map<String, Object> result = jdbcTemplate.queryForMap(sqlBuilder.toString());
            Double revenue = ((Number) result.get("total_revenue")).doubleValue();
            Double budget = ((Number) result.get("total_budget")).doubleValue();
            return new DirectorStats(null, movieIds.size(), revenue, budget, revenue - budget);
        } catch (Exception e) {
            return new DirectorStats(null, 0, 0.0, 0.0, 0.0);
        }
    }

    //helper method for task 2 
    private Object[] extractMovieParams(JsonObject movie) {
        return new Object[]{
            getStringValue(movie, "imdb_id"),
            getDoubleValue(movie, "vote_average"),
            getIntValue(movie, "vote_count"),
            Date.valueOf(getStringValue(movie, "release_date")),
            getDoubleValue(movie, "revenue"),
            getDoubleValue(movie, "budget"),
            getIntValue(movie, "runtime")
        };
    }

    private String getStringValue(JsonObject json, String key) {
        return json.containsKey(key) && !json.isNull(key)
                ? json.getString(key)
                : "";
    }

    private double getDoubleValue(JsonObject json, String key) {
        return json.containsKey(key) && !json.isNull(key)
                ? json.getJsonNumber(key).doubleValue()
                : 0.0;
    }

    private int getIntValue(JsonObject json, String key) {
        return json.containsKey(key) && !json.isNull(key)
                ? json.getJsonNumber(key).intValue()
                : 0;
    }
}
