package vttp.batch5.paf.movies.models;

public class DirectorStats {

    private String director_name;
    private Integer Movies_count;
    private Double total_revenue;
    private Double total_budget;
    private Double profit_loss;

    public DirectorStats() {
    }

    public DirectorStats(String director_name, Integer movies_count, Double total_revenue, Double total_budget,
            Double profit_loss) {
        this.director_name = director_name;
        Movies_count = movies_count;
        this.total_revenue = total_revenue;
        this.total_budget = total_budget;
        this.profit_loss = profit_loss;
    }

    public String getDirector_name() {
        return director_name;
    }

    public void setDirector_name(String director_name) {
        this.director_name = director_name;
    }

    public Double getTotal_revenue() {
        return total_revenue;
    }

    public void setTotal_revenue(Double total_revenue) {
        this.total_revenue = total_revenue;
    }

    public Double getTotal_budget() {
        return total_budget;
    }

    public void setTotal_budget(Double total_budget) {
        this.total_budget = total_budget;
    }

    public Double getProfit_loss() {
        return profit_loss;
    }

    public void setProfit_loss(Double profit_loss) {
        this.profit_loss = profit_loss;
    }

    public Integer getMovies_count() {
        return Movies_count;
    }

    public void setMovies_count(Integer movies_count) {
        Movies_count = movies_count;
    }

}
