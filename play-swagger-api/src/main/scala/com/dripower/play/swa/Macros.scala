package swagger.api

import java.io.FileWriter
import scala.reflect.macros.whitebox._
import com.dripower.play.route._
import org.joda.time._

class Macros(val c: Context) {
  import c.universe._
  def api[C: c.WeakTypeTag](a:c.Expr[Any]) = {
    val controller = c.weakTypeTag[C].tpe
    val ms = weakTypeTag[C].tpe.decls.collect {
      case m: MethodSymbol if !m.isConstructor && isSwaAction(m)  => m
    }.toList
    val (routes,apis) = extractData(ms)
    q"$apis"
  }

  private def extractData(list:List[MethodSymbol]) = {
    import scala.collection.mutable.ListBuffer
    val routes = new ListBuffer[PlayRoute]()
    val apis = new ListBuffer[String]()
    for(m <- list) {
      val r = extractRoute(m)
      val api = extractApi(m,r)
      routes += r
      apis += toJSONStr(api)
    }
    (routes.toList, apis.toList)
  }

   private def toJSONStr(api:Map[String,Any],tab:String = " "):String = {
    api.map{
      case (k,v:String) => "\"" + k + "\"" + ":" +  "\"" + v + "\""
      case (k,v:Map[String,Any]) => "\"" + k + "\"" + ":" +toJSONStr(v)
    }.mkString("{", ",", "}")
  }


  private def extractApi(m: MethodSymbol,r:PlayRoute) = {
    val typeParams = m.returnType.typeArgs
    val req = extractMember(typeParams(0))
    val res =  extractMember(typeParams(1))
    val api:Map[String,Any] = Map(("route",r.requestPath),("method",r.method),("req",req),("res",res))
    api
  }

  private def extractMember(t:c.universe.Type):Map[String,Any] = {
    if(isList(t)) { // 用于t直接为List类型
      Map(("List",extractMember(t.typeArgs(0))))
    } else {
      t.members.collect {
        case m: MethodSymbol if m.isCaseAccessor =>
          if(isBasicType(m.returnType)) {
            (m.name.toString,dealMultiplyName(m.returnType.toString))
          } else if (isList(m.returnType)) { // 用于成员含有List类型
            val t = m.returnType.typeArgs(0)
            if(isBasicType(t)) { // List的基本类型，如List[String]
              (m.name.toString,m.returnType.toString)
            } else {
              (s"${m.name.toString}:${dealMultiplyName(m.returnType.toString)}",extractMember(t)) // List的对象类型，如List[Person]
            }
          }
          else { //对象包对象的情况
            (m.name.toString,extractMember(m.returnType))
          }

      }.toMap
    }
  }

  private def isList(t:c.universe.Type):Boolean = {
    val s = t.toString
    val tStr = s.contains("[") match {
      case true => s.substring(0,s.indexOf("["))
      case false => s
    }
    tStr.equals("List")
  }

  /**
   *  判断是否为基本类型，这里把Option类型和Date类型，默认为基本类型
   */
  private def isBasicType(t:c.universe.Type):Boolean = {
    val basicType = List(
      "Long",
      "String",
      "Option",
      "Boolean",
      "Int",
      "Double",
      "Float",
      "Char",
      "Short",
      "Byte",
      "Unit",
      "org.joda.time.DateTime"
    )
    var s = t.toString
    var tStr = if (s.contains("[")) {
      s.substring(0,s.indexOf("["))
    } else {
      s
    }
    basicType.contains(tStr)
  }

  private def extractRoute(m: MethodSymbol):PlayRoute = {
    val method = if (isPostSwaAction(m)) PlayRoute.POST else PlayRoute.GET
    val actionPath = m.fullName
    val requestPath = extractRequestPath(actionPath)
    PlayRoute(
      method,
      requestPath,
      actionPath
    )
  }

  private def dealMultiplyName(name:String):String = {
    name.contains("[") match {
      case true => dealWithBracket(name)
      case false => name.substring(name.lastIndexOf(".") + 1, name.length)
    }
  }

  private def dealWithBracket(s:String):String = {

    def deal() = {
      val c =  s.substring(s.lastIndexOf(".")+1, s.indexOf("]"))
      val pre = s.substring(0,s.indexOf("["))
      s"${pre}[${c}]"
    }
    s.contains(".") match {
      case true => deal()
      case false => s
    }

  }

  private def extractRequestPath(fullName:String):String = {
    val strs = fullName.split("\\.")
    val actionName = strs(strs.length-1)
    val controllerName = strs(strs.length-2)
    val lastPackageName = strs(strs.length-3)
    s"${lastPackageName}-${controllerName}-${actionName}"
  }


  /**
   * 从所有的方法中选择返回值为 SwaAction的方法，因为那才是我们需要的方法
   * 这里使用的是字符串匹配的方式，通过匹配方法返回值是否是 “com.dripower.play.swa.SwaAction”
   * 有点low,最好的方式是通过类型来进行判断，但是目前不会，后期可以优化。
   */
  private def isSwaAction(m: MethodSymbol):Boolean = {
    isPostSwaAction(m) || isGetSwaAction(m)
  }


  private def isPostSwaAction(m: MethodSymbol):Boolean = {
    val returnTypeStr = m.returnType.toString
    val i =  returnTypeStr.indexOf("[")
    if (i == -1){
      false
    } else {
      val swaActionStr = returnTypeStr.substring(0,i)
      if(swaActionStr.equals("com.dripower.play.swa.PostSwaAction")){
        true
      }else {
        false
      }
    }
  }

  private def isGetSwaAction(m: MethodSymbol):Boolean = {
    val returnTypeStr = m.returnType.toString
    val i =  returnTypeStr.indexOf("[")
    if (i == -1){
      false
    } else {
      val swaActionStr = returnTypeStr.substring(0,i)
      if(swaActionStr.equals("com.dripower.play.swa.GetSwaAction")){
        true
      }else {
        false
      }
    }
  }

}
