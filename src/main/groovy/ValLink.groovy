/****************************************************************************************
 Extension Name: EXT010MI/ValLink
 Type: ExtendM3Transaction
 Script Author: Patrick BEAUDOUIN
 Date: 2024-01-01
 Description:
 * Validate link Circles and WashOut

 Revision History:
 Name                   Date             Version          Description of Changes
 PBEAUDOUIN             2024-01-01       1.0              CRO01 Management Circles and WashOu- Create ValLink transaction
 PBEAUDOUIN             2024-04-22       1.1              CRO01 Management Circles and WashOut - Change for approval
 PBEAUDOUIN             2024-04-22       1.2              CRO01 Management Circles and WashOut - add control lktp to calculate the ref price
 ******************************************************************************************/

import java.time.LocalDateTime

public class ValLink extends ExtendM3Transaction {
  private final MIAPI mi
  private final DatabaseAPI database
  private final LoggerAPI logger
  private final ProgramAPI program
  private final UtilityAPI utility
  private final MICallerAPI miCaller
  private Integer currentCompany
  private Integer lkid
  private Integer lktp
  private Integer stat
  private Integer instat

  /*
* Transaction EXT010MI/ValLink Interface
* @param mi - Infor MI Interface
* @param database - Infor Database Interface
* @param logger - Infor Logging Interface
* @param Program - Infor program Interface
* @param utility - utility Interface
* @param miCaller - Infor miCaller Interface
*/


  public ValLink(MIAPI mi, DatabaseAPI database, LoggerAPI logger, ProgramAPI program, UtilityAPI utility, MICallerAPI miCaller) {
    this.mi = mi
    this.database = database
    this.logger = logger
    this.program = program
    this.miCaller = miCaller
    this.utility = utility
  }

  public void main() {
    lkid = 0
    instat = 0
    stat = 0
    lktp = 0
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
    stat = 0
    String uyagst = ""
    String past = ""
    double puqt
    double agqt
    int sagl = 0
    String agst = ""
    double rfpr
    double pucd
    double sacd
    double olagpr
    double ajpupr
    currentCompany = (Integer) program.getLDAZD().CONO

    if (mi.in.get("LKID") != null) {
      lkid = mi.in.get("LKID")
    } else {
      mi.error("Identifiant lien obligatoire")
      return
    }

    if (mi.in.get("STAT") != null) {
      instat = mi.in.get("STAT") as Integer
    } else {
      mi.error("Statut obligatoire")
      return
    }

    LocalDateTime timeOfCreation = LocalDateTime.now()
    DBAction ext010Query = database.table("EXT010").selection("EXSUNO", "EXAGNB", "EXGRPI", "EXOBV1", "EXOBV2", "EXOBV3", "EXOBV4", "EXFVDT", "EXCUNO", "EXAGNO", "EXFDAT", "EXSTDT", "EXPREX", "EXUWO1", "EXUWO2", "EXUWO3", "EXUWO4", "EXRFPR", "EXSTAT","EXLKTP").index("00").build()
    DBContainer ext010Request = ext010Query.getContainer()
    ext010Request.set("EXCONO", currentCompany)
    ext010Request.set("EXLKID", lkid)

    //Record exists
    if (!ext010Query.read(ext010Request)) {
      mi.error("L'enregistrement n'existe pas")
      return
    } else {
      stat = (Integer) ext010Request.getInt("EXSTAT")
      suno = ext010Request.get("EXSUNO")
      agnb = ext010Request.get("EXAGNB")
      grpi = ext010Request.getInt("EXGRPI")
      obv1 = ext010Request.get("EXOBV1")
      obv2 = ext010Request.get("EXOBV2")
      obv3 = ext010Request.get("EXOBV3")
      obv4 = ext010Request.get("EXOBV4")
      fvdt = ext010Request.getInt("EXFVDT")
      cuno = ext010Request.get("EXCUNO")
      agno = ext010Request.get("EXAGNO")
      fdat = ext010Request.getInt("EXFDAT")
      stdt = ext010Request.getInt("EXSTDT")
      prex = ext010Request.get("EXPREX")
      uwo1 = ext010Request.get("EXUWO1")
      uwo2 = ext010Request.get("EXUWO2")
      uwo3 = ext010Request.get("EXUWO3")
      uwo4 = ext010Request.get("EXUWO4")
      lktp = ext010Request.getInt("EXLKTP")
      rfpr = (Double) ext010Request.get("EXRFPR")
    }

    if (instat == 20 && stat >= 20) {
      mi.error("Statut : " + stat + "   Lien déjà Actif")
      return
    }

    if (instat <= stat && stat > 20) {
      mi.error("Statut : " + stat + "   ne permet le passage au statut : " + instat)
      return
    }

    if (instat == 20) {

      //Check Customer exist
      if (cuno != null) {
        DBAction ocusmaQuery = database.table("OCUSMA").index("00").selection("OKSTAT").build()
        DBContainer ocusmaRequest = ocusmaQuery.getContainer()
        ocusmaRequest.set("OKCONO", currentCompany)
        ocusmaRequest.set("OKCUNO", cuno)
        if (!ocusmaQuery.read(ocusmaRequest)) {
          mi.error("Le Client " + cuno + " n 'existe pas")
          return
        }
        if (ocusmaRequest.get("OKSTAT") != "20") {
          mi.error("Statut Client " + ocusmaRequest.get("OKSTAT") + " non autorisé")
          return
        }
      } else {
        mi.error("Code Client obligatoire")
        return
      }
      //Check supplier exist
      if (suno != null) {
        DBAction cidmasQuery = database.table("CIDMAS").selection("IDSTAT").index("00").build()
        DBContainer cidmasRequest = cidmasQuery.getContainer()
        cidmasRequest.set("IDCONO", currentCompany)
        cidmasRequest.set("IDSUNO", suno)
        if (!cidmasQuery.read(cidmasRequest)) {
          mi.error("Le Fournisseur" + suno + " n'existe pas")
          return
        }

        if (cidmasRequest.get("IDSTAT") != "20") {
          mi.error("Statut Fournisseur " + cidmasRequest.get("IDSTAT") + " non autorisé")
          return
        }
      } else {
        mi.error("Code Fournisseur obligatoire")
        return
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

        if (past != "40") {
          mi.error("Contrat d'achat non Actif Statut : " + past)
          return
        }

      }

      //Check  Purchase agreement - line exist
      DBAction mpagrlQuery = database.table("MPAGRL").selection("AIAGQT", "AIPUQT", "AISAGL", "AIPUCD").index("00").build()
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
        puqt = (Double) mpagrlRequest.get("AIPUQT")
        agqt = (Double) mpagrlRequest.get("AIAGQT")
        pucd = (Double) mpagrlRequest.get("AIPUCD")
        if (pucd == 0d) {
          pucd = 1
        }
        if (sagl > 20) {
          mi.error("Ligne de contrat achat non Actif Statut : " + sagl)
          return
        } else if (puqt >= agqt) {
          mi.error("Ligne de contrat achat fermée")
          return
        }

      }

      Closure<?> oagrheReader = { DBContainer oagrheResult ->
        uyagst = oagrheResult.get("UYAGST")
        if (uyagst != "20") {
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

      if (!oagrheQuery.readAll(oagrheRequest, 3, 1, oagrheReader)) {
        mi.error("Contrat de vente  n'existe pas AGNO nv: " + agno + " CUNO : " + cuno + " STDT :" + stdt)
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
        mi.error("Ligne de contrat de vente  n'existe pas AGNO : " + agno + " OBV1 : " + uwo1 + " OBV2 : " + uwo2 + " OBV3 : " + uwo3 + " OBV4 : " + uwo4 + " FDAT : " + fdat + " STDT : " + stdt + " ¨PREX : " + prex)
        return
      } else {
        agst = oagrlnRequest.get("UWAGST")

        if (agst != "20") {
          mi.error("Ligne de contrat vente  appels non autorisés Statut : " + agst)
          return
        }
      }
      if (rfpr <= 0) {
        mi.error("Le prix de référence doit etre supérieur à 0")
        return
      } else {

        // Check Price OIS062
        DBAction oagrprQuery = database.table("OAGRPR").selection("OLAGPR", "OLSACD").index("00").build()
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
          mi.error("Le prix de vente  n'existe pas AGNO : " + agno + " CUNO " + cuno + " OBV1 : " + uwo1 + " OBV2 : " + uwo2 + " OBV3 : " + uwo3 + " OBV4 : " + uwo4 + " FDAT : " + fdat + " STDT : " + stdt + " ¨PREX : " + prex)
          return
        } else {
          olagpr = (double) oagrprRequest.get("OLAGPR")
          sacd = (double) oagrprRequest.get("OLSACD")
          if (sacd == 0d) {
            sacd = 1
          }
          if (lktp != 3 && (rfpr / sacd) > olagpr) {
            mi.error("Le prix de référence ne doit pas être supérieur au prix du contrat de vente : " + (olagpr * sacd))
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
          mi.error("Le prix d'achat  n'existe pas AGNB : " + agnb + " OBV1 : " + obv1 + " OBV2 : " + obv2 + " OBV3 : " + obv3 + " OBV4 : " + obv4 + " GRPI : " + grpi + " SUNO : " + suno + " FVDT : " + fvdt)
          return
        } else {
          ajpupr = (double) mpagrpRequest.get("AJPUPR")

          if (lktp != 3 && (rfpr / pucd) > ajpupr) {
            mi.error("Le prix de référence ne doit être supérieur au prix du contrat d'achat' : " + (ajpupr * pucd))
            return
          }
        }
      }
    }

    Closure<?> ext010Updater = { LockedResult ext010LockedResult ->
      ext010LockedResult.setInt("EXSTAT", instat)
      ext010LockedResult.update()
    }
    ext010Query.readLock(ext010Request, ext010Updater)
  }
}
