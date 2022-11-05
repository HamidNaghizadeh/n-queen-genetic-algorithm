
public class Chromosome implements Cloneable {
  public int[] genes;
  private int costFunction;


  public  Chromosome(int n){
    genes=new int[n];
  }

  public Chromosome(int[] a){
    genes=a;
  }

  public Chromosome(int[] genes,int costFunction) {
    this.genes=new int[genes.length];
    for (int i=0;i<genes.length;i++){
      this.genes[i]=genes[i];
    }
    this.costFunction=costFunction;
  }

  public Chromosome(Chromosome chrom){
    this.setGenes(chrom.getGenes());
    this.setCostFunction(chrom.getCostFunction());
  }

  public int[] getGenes() {
    return this.genes;
  }

  public int getCostFunction(){
    return this.costFunction;
  }

  public void setGenes(int[] genes) {
    this.genes = genes;
  }

  public void setCostFunction(int costFunction) {
    this.costFunction = costFunction;
  }

  @Override
  protected Object clone() throws CloneNotSupportedException {
    Chromosome chrome = (Chromosome) super.clone();
    for (int i=0;i<genes.length;i++){
      chrome.genes[i]=this.genes[i];
    }
    return chrome;
  }

  public Chromosome(){}

}
