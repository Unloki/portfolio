import scala.Array.ofDim

object StartSudoku {
  private var numberGrid = 0
  def main(args: Array[String]): Unit = {
    if(args.length > 0)
      this.numberGrid = args(0).toInt
    //SUdoku de taille 9x9
    val N = 9
    val modele = ofDim[Int](N,N)
    var sudoku = new Sudoku(modele)
    //Si on precise le nombre de grille genere en argument
    //Sinon, par defaut on genere 5 grill (default parameter)
    if(numberGrid > 0)
      sudoku = new Sudoku(modele, numberGrid)
    var gridToGenerate = List[Array[Array[Int]]]()

    var gridNUmber = 0
    gridToGenerate = sudoku.solver()
    for (gridGenerate <- gridToGenerate){
      gridNUmber = gridNUmber+1
      println("\n-----------GRID N°"+gridNUmber+"-----------")
      var vue = new SudokuVue(gridGenerate)
      print(vue)
    }
    //On va resoudre la grille de sudoku n°1
    val sudokuTest = new Sudoku(gridToGenerate(0))
    var gridNumberToSolve = 0
    val vue = new SudokuVue(sudokuTest.solver(true)(0))
    println("\n--------RESOLVE GRID N°"+(gridNumberToSolve+1)+"--------")
    print(vue)

    //On va resoudre la sudoku de l'énoncé
    val ennonceGille = Array(
      Array(5, 3, 0, 0, 7, 0, 0, 0, 0),
      Array(6, 0, 0, 1, 9, 5, 0, 0, 0),
      Array(0, 9, 8, 0, 0, 0, 0, 6, 0),
      Array(8, 0, 0, 0, 6, 0, 0, 0, 3),
      Array(4, 0, 0, 8, 0, 3, 0, 0, 1),
      Array(7, 0, 0, 0, 2, 0, 0, 0, 6),
      Array(0, 6, 0, 0, 0, 0, 2, 8, 0),
      Array(0, 0, 0, 4, 1, 9, 0, 0, 5),
      Array(0, 0, 0, 0, 8, 0, 0, 7, 9)
    )
    // j'aurais pus utiliser le meme systeme que pour l'echequier pour ajouter les cases (avec update)
    // qui donnerait = sudoku((1,2)) = 3
    // Mais je trouve plus rapide de creer directement la matrice avec les valeurs
    val sudokuEnonce= new Sudoku(ennonceGille)
    val vueEnonce = new SudokuVue(sudokuEnonce.solver(true)(0))
    println("\n--------RESOLVE GRID ENONCE--------")
    print(vueEnonce)

  }
}
class Sudoku(startConfig_ : Array[Array[Int]], numberGrid_ :Int = 5) {
  //5 Grille par defaut
  val startConfig = startConfig_;
  val N = startConfig_.length;
  val sizeQuadrant = getSizeQuadrant()
  val numberGrid = numberGrid_

  def this() {
    this(ofDim[Int](9,9));
    println("\n Pas de nombre de côté précisé !\n")
  }
  def getSizeQuadrant(): Int = {
    var result = 0;
    for( i <- 0 to N -1) {
      if (i > 0) {
        if((this.N % i) == 0)
          result = i
      }
    }
    return result
  }
  def solver(SolveASudoku_ : Boolean =  false): List[Array[Array[Int]]] ={
    var workGrid = Array.fill(N, N)(0)
    var result = List[Array[Array[Int]]]()
    if(SolveASudoku_){
      workGrid = this.startConfig
      fillXY(0,0)
      result = workGrid :: result
    }else{
      generateGridList()
    }

    def generateGridList(): Unit = {
      for (i <- 0 until this.numberGrid) {
        workGrid = Array.fill(N, N)(0)
        //On va "melanger" les cases en ajoutant des cases avec des valeur aléatoire sinon la 1er ligne = 1 2 3 4 5 6 7 8 9
        val RandNumber = scala.util.Random
        workGrid(0)(RandNumber.nextInt(this.N)) = RandNumber.nextInt(this.N)
        workGrid(0)(RandNumber.nextInt(this.N)) = RandNumber.nextInt(this.N)
        workGrid(1)(RandNumber.nextInt(this.N)) = RandNumber.nextInt(this.N)
        workGrid(1)(RandNumber.nextInt(this.N)) = RandNumber.nextInt(this.N)
        fillXY(0,0)
        RemoveRandomCase()
        result = workGrid :: result
      }
    }

    def RemoveRandomCase(): Unit = {
      val numberTime = 35;
      for (i <- 1 until numberTime) {
        val randXY = scala.util.Random
        val randXInt = randXY.nextInt(this.N)
        val randYInt = randXY.nextInt(this.N)
        workGrid(randXInt)(randYInt) = 0
      }
    }
    def fillXY(xy_ : Tuple2[Int, Int]): Unit = {
      fillXYUtil(xy_)
    }
    def fillXYUtil(xy_ : Tuple2[Int, Int]): Boolean = {
      val line = xy_._1;
      val column = xy_._2;
      // calcul de la position suivante
      var lineSuivante = 0
      var columnSuivante = 0
      if (column== this.N-1) {
        lineSuivante = line+1;
        columnSuivante=0
      }
      else{
        lineSuivante = line
        columnSuivante = column+1
      }
      if (line==N) {
        return true;
      }
      if (workGrid(line)(column)!=0) {
        return fillXYUtil((lineSuivante,columnSuivante))
      }
      else {
        for (valeur <- 1 until this.N+1) {
          if (isPossibleAt(valeur,line,column)){
            workGrid(line)(column) = valeur;
            val correct = fillXYUtil((lineSuivante,columnSuivante));
            if (correct) return true;
          }
        }
        workGrid(line)(column) = 0;
        false;
      }
    }

    def isPossibleAt(number_ : Int, x_ : Int, y_ : Int): Boolean = {
      return !isInColumn(number_,y_) && !isInLine(number_,x_) && !isInQuadrant(number_,x_,y_);
    }

    def isInColumn(valeur_ : Int, column_ : Int): Boolean = {
      for (line <- 0 until 9) {
        if (workGrid(line)(column_) == valeur_) return true
      }
      false
    }
    def isInLine(valeur_ : Int, line_ : Int): Boolean = {
      for (column <- 0 until 9) {
        if (workGrid(line_)(column) == valeur_) return true
      }
      false
    }
    def isInQuadrant(valeur_ : Int, line_ : Int, column_ : Int): Boolean = {
      val pointGauche = this.sizeQuadrant * (column_ / this.sizeQuadrant)
      val pointHaut = this.sizeQuadrant * (line_ / this.sizeQuadrant)
      for (c <- pointGauche until pointGauche + this.sizeQuadrant) {
        for (l <- pointHaut until pointHaut + this.sizeQuadrant) {
          if (workGrid(l)(c) == valeur_) return true
        }
      }
      false
    }
    // On retourne la liste de grill ou le sudoku resolu
    result;
  }
}

class SudokuVue(startConfig_ : Array[Array[Int]]){
  private val size = startConfig_.length
  private var modele = startConfig_
  private var sizeQuadrant = getSizeQuadrant()

  def getSizeQuadrant(): Int = {
    var result = 0;
    for( i <- 0 to size -1) {
      if (i > 0) {
        if((this.size % i) == 0)
          result = i
      }
    }
    return result
  }

  override def toString(): String = {
    var AllRow: String = " "+Ansi.UNDERLINED+"                       \n"
    for (i <- 0 to startConfig_.length - 1) {
      AllRow = AllRow + Gameline(i)
    }
    return AllRow;
  }
  def Gameline(currentCol_ : Int): String ={
    var tempLine: String = ""
    val currentRow = currentCol_;
    if((currentRow+1) % this.sizeQuadrant == 0 || currentCol_ ==this.size-1){
      tempLine = tempLine + Ansi.UNDERLINED
    }else{
      tempLine =   tempLine +Ansi.RESET
    }
    for( i <- 0 to this.size -1) {
      if (i > 0) {
        val modulo = (i % this.sizeQuadrant)
        if(modulo == 0){
          tempLine = tempLine + " | " +this.modele(currentCol_)(i)
        }else {
          if (this.modele(currentCol_)(i) != null) {
            if(this.modele(currentCol_)(i) == 0){
              tempLine = tempLine + "  "
            }else{
              tempLine = tempLine + " " + this.modele(currentCol_)(i)
            }
          } else {
            tempLine = tempLine+ " " ;
          }
        }
        if (i == this.size - 1) {
          tempLine = tempLine + " |" + Ansi.RESET;
        }
      }else{
        if(this.modele(currentCol_)(i) == 0) {
          tempLine = tempLine +  "|  "
        }else{
          tempLine = tempLine +  "| "+ this.modele(currentCol_)(i)        }
        }
    }
    return tempLine + Ansi.RESET + "\n";
  }
}
object Ansi {
  def reset() {
    println(Ansi.RESET)
  }

  val RESET: String ="\u001B[0m"

  val BLACK_B: String = "\u001B[40m"

  val RED: String = "\u001B[31m"

  val RED_B: String = "\u001B[41m"

  val GREEN: String = "\u001B[32m"

  val GREEN_B: String = "\u001B[42m"

  val YELLOW: String = "\u001B[33m"

  val YELLOW_B: String = "\u001B[43m"

  val BLUE: String = "\u001B[34m"

  val BLUE_B: String = "\u001B[44m"

  val MAGENTA: String = "\u001B[35m"

  val MAGENTA_B: String = "\u001B[45m"

  val CYAN: String = "\u001B[36m"

  val CYAN_B: String = "\u001B[46m"

  val WHITE: String = "\u001B[37m"

  val WHITE_B: String = "\u001B[47m"

  val UNDERLINED: String = "\u001B[4m"

  val BLACK: String = "\u001B[30m"
}