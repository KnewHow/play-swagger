package swagger.api


import java.io.FileWriter
import scala.reflect.macros.whitebox._

class Macros(val c: Context) {
  import c.universe._
  def api[C: c.WeakTypeTag](a:c.Expr[Any]) = {
    val controller = c.weakTypeTag[C].tpe
    val ms = weakTypeTag[C].tpe.decls.collect {
      case m: MethodSymbol if !m.isConstructor  => m
    }
    for(m <- ms) {
      println(m.returnType.toString)
    }
    println("ms"+ms)
    c.Expr[Unit](q""" println("Hello macro")""")
  }

}
