import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Random;


public class GeneticAlgorithm {

  // you can change this const variables and it will apply to all project, actually you can control project from this section.
  private static final int POP_SIZE=100;
  private static final int N_QUEEN=32;
  private static final float MUTATION_RATE = 0.65f;
  private static final int MAX_GENERATION=10000;
  private Chromosome[] population=new Chromosome[POP_SIZE];
  private Chromosome[] populationCopy =new Chromosome[POP_SIZE];
  private Chromosome[] offSprings=new Chromosome[POP_SIZE/2];

  private Random rnd=new Random();

  public void startProcess(){
    initializePopulation();
    System.out.println("Evolution in progress...");
    for (int i=0;i<MAX_GENERATION;i++){
      evolve();
      writeRecordInFile(i,minimumCostFunction());
      if(population[0].getCostFunction()==0){
        System.out.println("a solution found, it's:  " + Arrays.toString(population[0].genes));
        System.exit(0);
      }
    }
    System.out.println("Bad News, This Kind of creature lived " + MAX_GENERATION + " Generation in our world but it never could reach our expects, so we want to extinct this creature :|");
  }

  private void initializePopulation() {

    for (int i = 0; i < POP_SIZE; i++) {
      Chromosome newOne = new Chromosome(N_QUEEN);
      randomizeGenes(newOne);
      population[i] = new Chromosome(newOne);
    }
  }


  private void evolve(){

    Chromosome[] parents= parentSelection();
    populationCopy = deepCopyPopulation(population);
    crossOver(parents);
    //writeRecordInFile("C");       // if i want to save cost function records for each generation by using crossover, i use this line of code.
    mutation();
    //writeRecordInFile("M");      //  if i want to save cost function records for each generation by using mutation, i use this line of code.
    survivalSelection();
  }


  private int costFunction(Chromosome chrom){
    int conflict=0;
    int p=1;

    for (int i=0;i<N_QUEEN;i++){
      for (int j=i+1;j<N_QUEEN;j++){
        if (j-i==chrom.genes[i] - chrom.genes[j] || j-i== chrom.genes[j]-chrom.genes[i]){
          conflict++;
        }
      }

      if (i==0){
        continue;
      }
      for (int k=i-1;k>=0;k--){
          if (i-k==chrom.genes[k] - chrom.genes[i] || i-k==chrom.genes[i]-chrom.genes[k]){
            conflict++;
          }
      }
    }
    return conflict;
  }

  private Chromosome[] parentSelection(){

    for (Chromosome chrom:population){
      chrom.setCostFunction(costFunction(chrom));
    }

    Chromosome[] selectedParents=new Chromosome[POP_SIZE/2];
    Arrays.sort(population, new sortByRoll());

    // selects 90% of parents by cost function value.
    int byCostFunction = Math.round((POP_SIZE/2)*0.9f);
    for (int i=0;i<byCostFunction;i++){
      selectedParents[i]=new Chromosome(population[i].genes,population[i].getCostFunction());
    }

    // selects 10% of parents randomly among of remaining chromosomes.
    int randomSelect= (POP_SIZE/2) - byCostFunction;
    for (int i=0;i<randomSelect;i++){
      int randomIndex = rnd.nextInt(POP_SIZE-45)+45;
      selectedParents[byCostFunction+i]=new Chromosome(population[i].genes,population[i].getCostFunction());
    }
    return selectedParents;
  }

  // this function selects chromosome pairs from parents and send them to another function to do crossover.
  private void crossOver(Chromosome[] parents){

    for (int i=0;i<parents.length;i+=2){
      doCrossover(parents[i],parents[i+1],i);
    }
  }

  private void doCrossover(Chromosome parent1, Chromosome parent2, int index){

    final int pos=rnd.nextInt(N_QUEEN-1)+1;
    Chromosome parent1Copy=new Chromosome(parent1.genes,parent1.getCostFunction());
    Chromosome parent2Copy=new Chromosome(parent2.genes,parent2.getCostFunction());
    int counterP1=0;
    int counterP2=0;
    int lastEmptyIndexP1=pos;
    int lastEmptyIndexP2=pos;

    for (int i = pos; i < N_QUEEN; i++) {
      if (checkContaining(Arrays.copyOfRange(parent1.genes,0,pos),parent2.genes[i])){
        counterP1++;
        continue;
      }
      parent1.genes[lastEmptyIndexP1]=parent2.genes[i];
      lastEmptyIndexP1++;
    }

    for (int i = pos; i < N_QUEEN; i++) {
      if (checkContaining(Arrays.copyOfRange(parent2.genes,0,pos),parent1Copy.genes[i])){
        counterP2++;
        continue;
      }
      parent2.genes[lastEmptyIndexP2]=parent1Copy.genes[i];
      lastEmptyIndexP2++;
    }

    if (counterP1>0) {
      for (int i = 0; i < pos; i++) {
        if (!checkContaining(Arrays.copyOfRange(parent1.genes, 0, lastEmptyIndexP1), parent2Copy.genes[i])) {
          parent1.genes[lastEmptyIndexP1] = parent2Copy.genes[i];
          lastEmptyIndexP1++;
          counterP1--;
          if (counterP1==0){break;}
        }
      }
    }

    if (counterP2>0) {
      for (int i = 0; i < pos; i++) {
        if (!checkContaining(Arrays.copyOfRange(parent2.genes, 0, lastEmptyIndexP2), parent1Copy.genes[i])) {
          parent2.genes[lastEmptyIndexP2] = parent1Copy.genes[i];
          lastEmptyIndexP2++;
          counterP2--;
          if (counterP2==0){ break; }
        }
      }
    }
    offSprings[index]=parent1;
    offSprings[index+1]=parent2;
  }

  private void mutation(){

    ArrayList<Integer> numbers=new ArrayList<>();
    for (int i=0;i<POP_SIZE/2;i++){
      numbers.add(i);
    }
    int mutationPlan = Math.round((POP_SIZE/2)* MUTATION_RATE);
    for (int i=0;i<mutationPlan;i++){
      mutate(offSprings[numbers.remove(rnd.nextInt(numbers.size())).intValue()]);
    }

  }
  private void mutate(Chromosome offSpring){

    int n=rnd.nextInt(N_QUEEN-1);
    int m=rnd.nextInt(N_QUEEN-1);
    while(n==m){
      m=rnd.nextInt(N_QUEEN-1);
    }

    int temp=offSpring.genes[n];
    offSpring.genes[n]=offSpring.genes[m];
    offSpring.genes[m]=temp;
  }

  private void survivalSelection(){

    for (Chromosome chrom : offSprings){
      chrom.setCostFunction(costFunction(chrom));
    }

    for (Chromosome chrom : populationCopy){
      chrom.setCostFunction(costFunction(chrom));
    }

    int worldPop = POP_SIZE+(POP_SIZE/2);
    Chromosome[] allChromosomes= new Chromosome[worldPop];
    Chromosome[] allCms;
    System.arraycopy(populationCopy,0,allChromosomes,0,POP_SIZE);
    System.arraycopy(offSprings,0,allChromosomes,POP_SIZE,POP_SIZE/2);
    Arrays.sort(allChromosomes, new sortByRoll());
    ArrayList<Chromosome> allChromosomesAL= getArrayListFromArray(allChromosomes);
    ArrayList<Chromosome> withoutRepetitive=deleteRepetitive(allChromosomesAL);
    allCms= getArrayFromArrayList(withoutRepetitive);
    this.population= deepCopyPopulation(allCms);
  }


  // Auxiliary functions

  private void randomizeGenes(Chromosome chrom){
    Integer[] range=new Integer[N_QUEEN];
    for (int i=0;i<N_QUEEN;i++){
      range[i]=i+1;
    }
    ArrayList<Integer> numbers=new ArrayList<>(Arrays.asList(range));
    for (int i=0;i<chrom.genes.length;i++){
      int randIndex=numbers.remove(rnd.nextInt(numbers.size())).intValue();
      chrom.genes[i]=randIndex;
    }
  }
  
  private int getDiversity(){
    int counter=0;
    for (Chromosome chromosome1 : population) {
      for (Chromosome chromosome : population) {
        if (Arrays.equals(chromosome1.genes, chromosome.genes)) {
          if (chromosome==chromosome1){
            continue;
          }
          counter++;
        }
      }
    }
    return counter;
  }

  private ArrayList<Chromosome> deleteRepetitive(ArrayList<Chromosome> chrom){
    for (int i=0;i<chrom.size();i++) {
      for (int j=0;j<chrom.size();j++) {
        if (Arrays.equals(chrom.get(i).genes, chrom.get(j).genes)) {
          if (i==j){
            continue;
          }
          chrom.remove(j);
        }
      }
    }
    return chrom;
  }
  
  public void showChromosomes(){
    for (Chromosome pop:population){
      System.out.println(Arrays.toString(pop.genes) + " " + pop.getCostFunction());
    }
  }

  class sortByRoll implements Comparator<Chromosome>
  {
    // Used for sorting in ascending order of cost function
    public int compare(Chromosome a, Chromosome b)
    {
      return a.getCostFunction() - b.getCostFunction();
    }
  }

  private static boolean checkContaining(int[] arr, int toCheckValue)
  {
    boolean result = false;
    for (int element : arr) {
      if (element == toCheckValue) {
        result = true;
        break;
      }
    }
    return result;
  }

  private ArrayList<Chromosome> getArrayListFromArray(Chromosome[] array){
    ArrayList<Chromosome> arrayList = new ArrayList<>();
    for(Chromosome element:array) {
      arrayList.add(element);
    }
    return arrayList;
  }

  private Chromosome[] getArrayFromArrayList(ArrayList<Chromosome> arrayList){
    Chromosome[] chromes=new Chromosome[arrayList.size()];
    for (int i=0;i<chromes.length;i++){
      chromes[i]=arrayList.get(i);
    }
    return chromes;
  }

  private int minimumCostFunction(){
    int min=population[0].getCostFunction();
    for (int i=1;i<POP_SIZE;i++){
      if (population[i].getCostFunction() < min){
        min = population[i].getCostFunction();
      }
    }
    return min;
  }
  private int minimumCostFunction(Chromosome[] chromm){

    int min=chromm[0].getCostFunction();
    for (int i=1;i<chromm.length;i++){
      if (chromm[i].getCostFunction() < min){
        min = chromm[i].getCostFunction();
      }
    }
    return min;
  }

  private int costFunctionAverage(){
    int sum=0;
    for (int i=0;i<POP_SIZE;i++){
      sum+=population[i].getCostFunction();
    }
    return sum/POP_SIZE;
  }

  private Chromosome[] deepCopyPopulation(Chromosome[] chromosomes){
    Chromosome[] chromes=new Chromosome[POP_SIZE];
    for (int i=0;i<POP_SIZE;i++){
      chromes[i]=new Chromosome(chromosomes[i].genes,chromosomes[i].getCostFunction());
    }
    return chromes;
  }

  private void writeRecordInFile(int generation, int costFunction){
    try {
      PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter("C:\\\\Users\\\\Hamid\\\\Desktop\\\\generation-best-cost.txt", true)));
      out.println(generation + " " + costFunction );
      out.close();
    } catch (IOException e) {
      System.out.println("Something get wrong");
    }
  }

  private void writeRecordInFile(String type){
    for (Chromosome ch:offSprings){
      ch.setCostFunction(costFunction(ch));
    }
    int costFunction = minimumCostFunction(offSprings);
    try {
      if (type.equals("C")){
        PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter("C:\\\\Users\\\\Hamid\\\\Desktop\\\\crossover.txt", true)));
        out.println(costFunction );
        out.close();
      }
      else {
        PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter("C:\\\\Users\\\\Hamid\\\\Desktop\\\\mutation.txt", true)));
        out.println(costFunction );
        out.close();
      }

    } catch (IOException e) {
      System.out.println("Something get wrong");
    }
  }

}

