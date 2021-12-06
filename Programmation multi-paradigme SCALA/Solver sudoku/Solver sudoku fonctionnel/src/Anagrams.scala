import scala.collection.immutable.WrappedString
import scala.{Array => $}
object TestAnagram{
  def main(args: $[String]): Unit = {

    var anagram = new Anagram()
    var allAnagrams = anagram.anagrams("bonjour")
    (0 until allAnagrams.size).map(i =>  println(i))
  }
}

class Anagram() {

  def anagrams(chaine_ : String): Set[String] ={
    val chaine = chaine_
    var allLetter = (0 until chaine.size).map(v =>  chaine.charAt(v).toString())

    def angramsRec(result_ : List[String] ): List[String] ={
      println("0"+result_)
      var currentResult = result_
      def AddAnagrams(j_ : Int) : List[String] = {
        val valeurCase = j_
        var letterAlreadyT = List[String]()
        var result = List[String]()
        println("1"+allLetter(j_))
        if (!letterAlreadyT.contains(allLetter(j_))) {
          letterAlreadyT = allLetter(j_) ::letterAlreadyT
          var letterRemaining = currentResult
          println("2"+letterRemaining.size)
          var max = result.size
          println("2"+letterRemaining.patch(j_, Nil, 1))
          var letterRest = angramsRec(letterRemaining.patch(j_, Nil, 1).toList)
          (0 until letterRest.size).map(v =>   letterRest.toSeq(v)::result)
          (0 until max).map(v =>  result.updated(v,allLetter(j_)+result(v)))
        }
        return result
      }
      if (currentResult.size > 1) {
        val AnagramsToAdd = (0 until chaine.size).flatMap(AddAnagrams)
        return  AnagramsToAdd.toList
      }else{
        return result_
      }
    }
    var result = angramsRec(allLetter.toList)
    println("resul = "+result)
    return result.toSet
  }
}
