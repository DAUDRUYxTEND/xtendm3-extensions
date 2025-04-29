/****************************************************************************************
 Extension Name: EXT010MI/AddLink
 Type: ExtendM3Transaction
 Script Author: Patrick BEAUDOUIN
 Date: 2024-01-01
 Description:
 * Add link Circles and WashOut

 Revision History:
 Name                   Date             Version          Description of Changes
 PBEAUDOUIN             2024-01-01       1.0              CRO01 Management Circles and WashOu- Create AddLink transaction
 PBEAUDOUIN             2024-04-22       1.1              CRO01 Management Circles and WashOut - Change for approval
 ******************************************************************************************/

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

public class AddLink extends ExtendM3Transaction {
  private final MIAPI mi
  private final DatabaseAPI database
  private final LoggerAPI logger
  private final ProgramAPI program
  private final UtilityAPI utility
  private final MICallerAPI miCaller
  private Integer currentCompany
  private Integer lkid

  /*
  * Transaction EXT010MI/AddLink Interface
  * @param mi - Infor MI Interface
  * @param database - Infor Database Interface
  * @param logger - Infor Logging Interface
  * @param Program - Infor program Interface
  * @param utility - utility Interface
  * @param miCaller - Infor miCaller Interface
  */

  public AddLink(MIAPI mi, DatabaseAPI database, LoggerAPI logger, ProgramAPI program, UtilityAPI utility, MICallerAPI miCaller) {
    this.mi = mi
    this.database = database
    this.logger = logger
    this.program = program
    this.miCaller = miCaller
    this.utility = utility
  }

  public void main() {

    lkid = 0
    String suno = ""
    String agnb = ""
    int grpi = 0
    String obv1 = ""
    String obv2 = ""
    String obv3 = ""
    String obv4 = ""
    int fvdt = 0
    String uwdi = ""
    String cuno = ""
    String agno = ""
    int fdat = 0
    int stdt = 0
    String prex = ""
    String uwo1 = ""
    String uwo2 = ""
    String uwo3 = ""
    String uwo4 = ""
    int lktp = 0
    String lkrf = ""
    double lkqt
    double rfpr
    double olagpr
    double ajpupr
    int dudt = 0
    int ivdt = 0
    int stat = 10
    int chno = 1
    double puqt
    double agqt
    double pucd
    double sacd
    int sagl = 0
    String agst =""
    String uyagst = ""
    String past = ""

    currentCompany = (Integer) program.getLDAZD().CONO

    //Check supplier exist
    if (mi.in.get("SUNO") != null) {
      DBAction cidmasQuery = database.table("CIDMAS").selection("IDSTAT").index("00").build()
      DBContainer cidmasRequest = cidmasQuery.getContainer()
      cidmasRequest.set("IDCONO", currentCompany)
      cidmasRequest.set("IDSUNO", mi.in.get("SUNO"))
      if (!cidmasQuery.read(cidmasRequest)) {
        mi.error("Le Fournisseur" + mi.in.get("SUNO") + " n'existe pas")
        return
      }
      suno = mi.in.get("SUNO")
      if(cidmasRequest.get("IDSTAT")!="20" ){
        mi.error("Statut Fournisseur " + cidmasRequest.get("IDSTAT") + " non autorisé")
        return
      }
    } else {
      mi.error("Code Fournisseur obligatoire")
      return
    }


    if (mi.in.get("AGNB") != null) {
      agnb = mi.in.get("AGNB")
    } else {
      mi.error("Numero contrat achat obligatoire")
      return
    }

    if (mi.in.get("GRPI") != null) {
      grpi = mi.in.get("GRPI") as int
    } else {
      mi.error("Groupe contrat achat obligatoire")
      return
    }

    if (mi.in.get("OBV1") != null) {
      obv1 = mi.in.get("OBV1")
    } else {
      mi.error("OBV1 contrat achat obligatoire")
      return
    }

    if (mi.in.get("OBV2") != null) {
      obv2 = mi.in.get("OBV2")
    }

    if (mi.in.get("OBV3") != null) {
      obv3 = mi.in.get("OBV3")
    }

    if (mi.in.get("OBV4") != null) {
      obv4 = mi.in.get("OBV4")
    }

    if (mi.in.get("FVDT") == null) {
      mi.error("Date début contrat achat est obligatoire")
      return
    } else {
      fvdt = mi.in.get("FVDT") as int
      boolean checkDate = (Boolean) utility.call("DateUtil", "isDateValid", "" + fvdt, "yyyyMMdd")
      if (!checkDate) {
        mi.error("Format Date début contrat d'achat incorrect")
        return
      }

    }

    //Check  Purchase agreement - Status
    DBAction mpagrhQuery = database.table("MPAGRH").selection("AHPAST").index("00").build()
    DBContainer mpagrhRequest = mpagrhQuery.getContainer()
    mpagrhRequest.set("AHCONO", currentCompany)
    mpagrhRequest.set("AHSUNO", suno)
    mpagrhRequest.set("AHAGNB", agnb)

    if (!mpagrhQuery.read(mpagrhRequest)) {
      mi.error("Contrat achat  n'existe pas AGNB : " + agnb + " SUNO : " + suno)
      return
    } else {
      past = mpagrhRequest.get("AHPAST")

      if (past !="40" ){
        mi.error("Contrat d'achat non Actif Statut : " + past)
        return
      }

    }

    //Check  Purchase agreement - line exist

    DBAction mpagrlQuery = database.table("MPAGRL").selection("AIAGQT","AIPUQT","AISAGL","AIPUCD").index("00").build()
    DBContainer mpagrlRequest = mpagrlQuery.getContainer()
    mpagrlRequest.set("AICONO", currentCompany)
    mpagrlRequest.set("AISUNO", suno)
    mpagrlRequest.set("AIAGNB", agnb)
    mpagrlRequest.set("AIGRPI", grpi)
    mpagrlRequest.set("AIOBV1", obv1)
    mpagrlRequest.set("AIOBV2", obv2)
    mpagrlRequest.set("AIOBV3", obv3)
    mpagrlRequest.set("AIOBV4", obv4)
    mpagrlRequest.set("AIFVDT", fvdt)
    if (!mpagrlQuery.read(mpagrlRequest)) {
      mi.error("Ligne de contrat d' achat  n'existe pas AGNB : " + agnb + " SUNO : " + suno)
      return
    } else {
      sagl = (Integer) mpagrlRequest.get("AISAGL")
      puqt =  (Double) mpagrlRequest.get("AIPUQT")
      agqt =  (Double) mpagrlRequest.get("AIAGQT")
      pucd = (Double) mpagrlRequest.get("AIPUCD")
      if (pucd == 0d){
        pucd =1
      }
      if (sagl >20 ){
        mi.error("Ligne de contrat achat non Actif Statut : " + sagl)
        return
      } else if (puqt >= agqt){
        mi.error("Ligne de contrat achat fermée")
        return
      }

    }

    if (mi.in.get("UWDI") != null) {
      uwdi = mi.in.get("UWDI")
    }


    //Check Customer exist
    if (mi.in.get("CUNO") != null) {
      DBAction ocusmaQuery = database.table("OCUSMA").index("00").selection("OKSTAT").build()
      DBContainer ocusmaRequest = ocusmaQuery.getContainer()
      ocusmaRequest.set("OKCONO", currentCompany)
      ocusmaRequest.set("OKCUNO", mi.in.get("CUNO"))
      if (!ocusmaQuery.read(ocusmaRequest)) {
        mi.error("Le Client "+ mi.in.get("CUNO") + " n 'existe pas")
        return
      }
      cuno = mi.in.get("CUNO")
      if (ocusmaRequest.get("OKSTAT") != "20"){
        mi.error("Statut Client "+ocusmaRequest.get("OKSTAT") +" non autorisé")
        return
      }
    } else {
      mi.error("Code Client obligatoire")
      return
    }

    if (mi.in.get("AGNO") != null) {
      agno = mi.in.get("AGNO")
    } else {
      mi.error("Numero contrat de vente obligatoire")
      return
    }


    if (mi.in.get("FDAT") == null) {
      mi.error("From date est obligatoire")
      return
    } else {
      fdat = mi.in.get("FDAT") as int
      boolean checkDate = (Boolean) utility.call("DateUtil", "isDateValid", "" + fdat, "yyyyMMdd")
      if (!checkDate) {
        mi.error("Format Date From date incorrect")
        return
      }

    }

    if (mi.in.get("STDT") == null) {
      mi.error("Date de début contrat de vente est obligatoire")
      return
    } else {
      stdt = mi.in.get("STDT") as int
      boolean checkDate = (Boolean) utility.call("DateUtil", "isDateValid", "" + fdat, "yyyyMMdd")
      if (!checkDate) {
        mi.error("Format Date début contrat de vente incorrect")
        return
      }

    }

    if (mi.in.get("PREX") != null) {
      prex = mi.in.get("PREX")
      prex = String.format("%2s", prex)
    }


    if (mi.in.get("UWO1") != null) {
      uwo1 = mi.in.get("UWO1")
    } else {
      mi.error("OBV1 contrat ventet obligatoire")
      return
    }

    if (mi.in.get("UWO2") != null) {
      uwo2 = mi.in.get("UWO2")
    }

    if (mi.in.get("UWO3") != null) {
      uwo3 = mi.in.get("UWO3")
    }

    if (mi.in.get("UWO4") != null) {
      uwo4 = mi.in.get("UWO4")
    }


    Closure<?> oagrheReader = { DBContainer oagrheResult ->
      uyagst = oagrheResult.get("UYAGST")
      if (uyagst !="20"){
        mi.error("Contrat de vente non Actif Statut : " + uyagst)
        return
      }
    }


    //Check  OIS060 - Status
    ExpressionFactory oagrheExpression = database.getExpressionFactory("OAGRHE")
    oagrheExpression = oagrheExpression.le("UYSTDT", String.valueOf(stdt))
    oagrheExpression = oagrheExpression.and(oagrheExpression.ge("UYLVDT", String.valueOf(stdt)))
    DBAction oagrheQuery = database.table("OAGRHE").index("00").matching(oagrheExpression).selection("UYAGST").build()
    DBContainer oagrheRequest = oagrheQuery.getContainer()
    oagrheRequest.set("UYCONO", currentCompany)
    oagrheRequest.set("UYCUNO", cuno)
    oagrheRequest.set("UYAGNO", agno)

    if (!oagrheQuery.readAll(oagrheRequest,3,1,oagrheReader)) {
      mi.error("Contrat de vente  n'existe pas AGNO nv: " + agno + " CUNO : " + cuno + " STDT :"+ stdt)
      return
    }


    //Check  OIS060 - line exist

    DBAction oagrlnQuery = database.table("OAGRLN").selection("UWAGST").index("00").build()
    DBContainer oagrlnRequest = oagrlnQuery.getContainer()
    oagrlnRequest.set("UWCONO", currentCompany)
    oagrlnRequest.set("UWCUNO", cuno)
    oagrlnRequest.set("UWAGNO", agno)
    oagrlnRequest.set("UWFDAT", fdat)
    oagrlnRequest.set("UWSTDT", stdt)
    oagrlnRequest.set("UWPREX", prex)
    oagrlnRequest.set("UWOBV1", uwo1)
    oagrlnRequest.set("UWOBV2", uwo2)
    oagrlnRequest.set("UWOBV3", uwo3)
    oagrlnRequest.set("UWOBV4", uwo4)
    if (!oagrlnQuery.read(oagrlnRequest)) {
      mi.error("Ligne de contrat de vente  n'existe pas AGNO : " + agno + " OBV1 : " + uwo1 + " OBV2 : " + uwo2+ " OBV3 : " + uwo3+ " OBV4 : " + uwo4 + " FDAT : " + fdat+ " STDT : " + stdt+ " ¨PREX : " + prex)
      return
    }else {
      agst = oagrlnRequest.get("UWAGST")

      if (agst !="20"  ){
        mi.error("Ligne de contrat vente  appels non autorisés Statut : " + agst)
        return
      }
    }


    if (mi.in.get("LKTP") == null) {
      mi.error("Type lien obligatoire")
      return
    } else {
      lktp = mi.in.get("LKTP") as int

      if (lktp < 0 || lktp > 3) {
        mi.error("Type Lien incorrect (Valeurs possibles : 1-Cercles 2-Washout 3-ByPass)")
        return
      }

    }


    if (mi.in.get("LKRF") != null) {
      lkrf = mi.in.get("LKRF")
    }

    if (mi.in.get("LKQT") == null) {
      mi.error("Quantité obligatoire")
      return
    } else {
      lkqt = (double) mi.in.get("LKQT")
      if (lkqt<=0 ){
        mi.error("La quantité doit être supérieur à 0")
        return
      }
    }

    if (mi.in.get("RFPR") == null) {
      mi.error("Prix référence obligatoire")
      return
    } else {
      rfpr = (double) mi.in.get("RFPR")
      if (rfpr<=0 ){
        mi.error("Le prix de référence doit être supérieur à 0")
        return
      } else{

        // Check Price OIS062
        DBAction oagrprQuery = database.table("OAGRPR").selection("OLAGPR","OLSACD").index("00").build()
        DBContainer oagrprRequest = oagrprQuery.getContainer()
        oagrprRequest.set("OLCONO", currentCompany)
        oagrprRequest.set("OLCUNO", cuno)
        oagrprRequest.set("OLAGNO", agno)
        oagrprRequest.set("OLFDAT", fdat)
        oagrprRequest.set("OLSTDT", stdt)
        oagrprRequest.set("OLPREX", prex)
        oagrprRequest.set("OLOBV1", uwo1)
        oagrprRequest.set("OLOBV2", uwo2)
        oagrprRequest.set("OLOBV3", uwo3)
        oagrprRequest.set("OLOBV4", uwo4)
        oagrprRequest.set("OLQTYL", 0)
        if (!oagrprQuery.read(oagrprRequest)) {
          mi.error("Le prix de vente  n'existe pas AGNO : " + agno +" CUNO : "+ cuno +" OBV1 : " + uwo1 + " OBV2 : " + uwo2+ " OBV3 : " + uwo3+ " OBV4 : " + uwo4 + " FDAT : " + fdat+ " STDT : " + stdt+ " ¨PREX : " + prex)
          return
        }else {
          olagpr = (double) oagrprRequest.get("OLAGPR")
          sacd = (double) oagrprRequest.get("OLSACD")
          if (sacd==0d){
            sacd=1
          }
          if (lktp!=3 &&((rfpr/sacd) > olagpr )){
            mi.error("Le prix de référence ne doit pas être supérieur au prix du contrat de vente : " + (olagpr*sacd))
            return
          }
        }

        // Check Price PPS102
        DBAction mpagrpQuery = database.table("MPAGRP").selection("AJPUPR").index("00").build()
        DBContainer mpagrpRequest = mpagrpQuery.getContainer()
        mpagrpRequest.set("AJCONO", currentCompany)
        mpagrpRequest.set("AJSUNO", suno)
        mpagrpRequest.set("AJAGNB", agnb)
        mpagrpRequest.set("AJGRPI", grpi)
        mpagrpRequest.set("AJOBV1", obv1)
        mpagrpRequest.set("AJOBV2", obv2)
        mpagrpRequest.set("AJOBV3", obv3)
        mpagrpRequest.set("AJOBV4", obv4)
        mpagrpRequest.set("AJFVDT", fvdt)
        mpagrpRequest.set("AJFRQT", 0)

        if (!mpagrpQuery.read(mpagrpRequest)) {
          mi.error("Le prix d'achat  n'existe pas AGNB : " + agnb + " OBV1 : " + obv1 + " OBV2 : " + obv2+ " OBV3 : " + obv3+ " OBV4 : " + obv4 + " GRPI : " + grpi+ " SUNO : " + suno+ " FVDT : " + fvdt)
          return
        }else {
          ajpupr = (double) mpagrpRequest.get("AJPUPR")
          if (lktp!=3 &&((rfpr/pucd) > ajpupr  )){
            mi.error("Le prix de référence ne doit être supérieur au prix du contrat d'achat' : " + (ajpupr*pucd))
            return
          }
        }

      }
    }


    if (mi.in.get("DUDT") != null) {
      dudt = mi.in.get("DUDT") as int
      boolean checkDate = (Boolean) utility.call("DateUtil", "isDateValid", "" + dudt, "yyyyMMdd")
      if (!checkDate) {
        mi.error("Format Date d'échéance incorrect")
        return
      }
    } else {
      mi.error("Date d'échéance obligatoire")
      return
    }

    if (mi.in.get("IVDT") != null) {
      ivdt = mi.in.get("IVDT") as int
      boolean checkDate = (Boolean) utility.call("DateUtil", "isDateValid", "" + ivdt, "yyyyMMdd")
      if (!checkDate) {
        mi.error("Format Date de facture incorrect")
        return
      }
    } else {
      mi.error("Date Factjure obligatoire")
      return
    }

    //Retrieve new lkid
    executeCRS165MIRtvNextNumber("CE","1")

    LocalDateTime timeOfCreation = LocalDateTime.now()
    DBAction ext010Query = database.table("EXT010").index("00").build()
    DBContainer ext010Request = ext010Query.getContainer()
    ext010Request.set("EXCONO", currentCompany)
    ext010Request.set("EXLKID", lkid)
    if (!ext010Query.read(ext010Request)) {
      ext010Request.set("EXSUNO", suno)
      ext010Request.setInt("EXAGNB", agnb as Integer)
      ext010Request.set("EXOBV1", obv1)
      ext010Request.setInt("EXFVDT", fvdt as Integer)
      ext010Request.set("EXUWDI", uwdi)
      ext010Request.set("EXCUNO", cuno)
      ext010Request.set("EXAGNO", agno)
      ext010Request.setInt("EXFDAT", fdat as Integer)
      ext010Request.setInt("EXSTDT", stdt as Integer)
      ext010Request.set("EXUWO1", uwo1)
      ext010Request.set("EXGRPI", grpi)
      ext010Request.set("EXPREX", prex)
      ext010Request.set("EXOBV2", obv2)
      ext010Request.set("EXOBV3", obv3)
      ext010Request.set("EXOBV4", obv4)
      ext010Request.set("EXUWO2", uwo2)
      ext010Request.set("EXUWO3", uwo3)
      ext010Request.set("EXUWO4", uwo4)
      ext010Request.setInt("EXLKTP", lktp as Integer)
      ext010Request.set("EXLKRF",lkrf )
      ext010Request.setDouble("EXLKQT", lkqt as Double)
      ext010Request.setDouble("EXRFPR", (rfpr/pucd) as Double)
      ext010Request.setInt("EXDUDT", dudt as Integer)
      ext010Request.setInt("EXIVDT", ivdt as Integer)
      ext010Request.setInt("EXSTAT", stat as Integer)
      ext010Request.setInt("EXRGDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer)
      ext010Request.setInt("EXRGTM", timeOfCreation.format(DateTimeFormatter.ofPattern("HHmmss")) as Integer)
      ext010Request.setInt("EXLMDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer)
      ext010Request.setInt("EXCHNO", chno)
      ext010Request.set("EXCHID", program.getUser())
      ext010Query.insert(ext010Request)
      mi.outData.put("LKID", lkid as String)
      mi.write()

    } else {
      mi.error("L'enregistrement existe déjà")
      return
    }

  }

  /**
   * all CRS165MI.RtrnNextNumber
   * @parameter nbty (Type Serie)
   * @parameter nbid  (Serie)
   * @return
   * */

  private executeCRS165MIRtvNextNumber(String nbty, String nbid){
    Map<String, String> parameters = ["NBTY": nbty, "NBID": nbid]
    Closure<?> handler = { Map<String, String> response ->
      lkid = response.NBNR.trim() as Integer
      if (response.error != null) {
        return mi.error("Failed CRS165MI.RtvNextNumber: "+ response.errorMessage)
      }
    }
    miCaller.call("CRS165MI", "RtvNextNumber", parameters, handler)
  }

}
