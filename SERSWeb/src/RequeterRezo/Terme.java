package RequeterRezo;

/**
 * Terme connexe au mot requêté. Les seuls informations connues sont le nom et
 * le poids.
 */
public class Terme {

    /**
     * Nom du terme connexe.
     */
    protected final String terme;
    
    /**
     * Poids du terme connexe.
     */
    protected final double poids;

    
    protected Terme(String terme, double poids) {
        if(terme.contains("  "))
        {
            terme = terme.replaceAll("  ", " ");
        }
        this.terme = terme;
        this.poids = poids;
    }

    @Override
    public String toString() {
        return (this.getTerme() + "=" + this.getPoids());
    }

    public String getTerme() {
        return terme;
    }

    public double getPoids() {
        return poids;
    }
}
