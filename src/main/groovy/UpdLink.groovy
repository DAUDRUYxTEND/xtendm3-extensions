/****************************************************************************************
 Extension Name: EXT010MI/UpdLink
 Type: ExtendM3Transaction
 Script Author: Patrick BEAUDOUIN
 Date: 2024-01-01
 Description:
 * Update link Circles and WashOut

 Revision History:
 Name                   Date             Version          Description of Changes
 PBEAUDOUIN             2024-01-01       1.0              CRO01 Management Circles and WashOut - Create UpdLink transaction
 PBEAUDOUIN             2024-04-22       1.1              CRO01 Management Circles and WashOut - Change for approval
 ******************************************************************************************/
import java.time.LocalDateTime

public class UpdLink extends ExtendM3Transaction {
  private final MIAPI mi
  private final DatabaseAPI database
  private final LoggerAPI logger
  private final ProgramAPI program
  private final UtilityAPI utility
  private final MICallerAPI miCaller
  private Integer currentCompany
  private Integer lkid
  private Long txid
  private Long intxid
  private suppTXID
  /*
 * Transaction EXT010MI/UpdLink Interface
 * @param mi - Infor MI Interface
 * @param database - Infor Database Interface
 * @param logger - Infor Logging Interface
 * @param Program - Infor program Interface
 * @param utility - utility Interface
 * @param miCaller - Infor miCaller Interface
 */

  public UpdLink(MIAPI mi, DatabaseAPI database, LoggerAPI logger, ProgramAPI program, UtilityAPI utility, MICallerAPI miCaller) {
    this.mi = mi
    this.database = database
    this.logger = logger
    this.program = program
    this.miCaller = miCaller
    this.utility = utility
  }

  public void main() {
    suppTXID = ""
    lkid = 0
    intxid = 0
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
    int stat = 0
    int chno = 1
    double puqt
    double agqt
    double pucd
    double sacd
    int sagl = 0
    String agst = ""
    String uyagst = ""
    String past = ""
    txid = 0
    currentCompany = (Integer) program.getLDAZD().CONO
    if (mi.in.get("LKID") != null) {
      lkid = mi.in.get("LKID")
    } else {
      mi.error("Identifiant lien obligatoire")
      return
    }

    if (mi.in.get("LKRF") != null) {
      lkrf = mi.in.get("LKRF")
    }

    if (mi.in.get("LKQT") != null) {
      lkqt = (double) mi.in.get("LKQT")
      if (lkqt <= 0) {
        mi.error("La quantité doit être supérieur à 0")
        return
      }
    }

    if (mi.in.get("DUDT") != null) {
      dudt = mi.in.get("DUDT") as int
      boolean checkDate = (Boolean) utility.call("DateUtil", "isDateValid", "" + dudt, "yyyyMMdd")
      if (!checkDate) {
        mi.error("Format Date d'échéance incorrect")
        return
      }
    }

    if (mi.in.get("IVDT") != null) {
      ivdt = mi.in.get("IVDT") as int
      boolean checkDate = (Boolean) utility.call("DateUtil", "isDateValid", "" + ivdt, "yyyyMMdd")
      if (!checkDate) {
        mi.error("Format Date de facture incorrect")
        return
      }
    }

    if (mi.in.get("TXID") != null) {
      intxid = mi.in.get("TXID") as Long
    }

    LocalDateTime timeOfCreation = LocalDateTime.now()
    DBAction ext010Query = database.table("EXT010").selection("EXCONO", "EXLKID", "EXLKTP", "EXSUNO", "EXAGNB", "EXGRPI", "EXOBV1", "EXOBV2", "EXOBV3", "EXOBV4", "EXFVDT", "EXUWDI", "EXCUNO", "EXAGNO", "EXFDAT", "EXSTDT", "EXPREX", "EXUWO1", "EXUWO2", "EXUWO3", "EXUWO4", "EXSTAT", "EXTXID").index("00").build()
    DBContainer ext010Request = ext010Query.getContainer()
    ext010Request.set("EXCONO", currentCompany)
    ext010Request.set("EXLKID", lkid)

    //Record exists
    if (!ext010Query.read(ext010Request)) {
      mi.error("L'enregistrement n'existe pas")
      return
    } else {
      suno = ext010Request.get("EXSUNO")
      lktp = ext010Request.getInt("EXLKTP")
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
      txid = ext010Request.getLong("EXTXID")
      stat = ext010Request.getInt("EXSTAT")
      // Check Status <20
      if (stat >= 20) {
        mi.error("Statut : " + stat + " modification interdite")
        return
      }
    }
    if (mi.in.get("RFPR") != null) {
      rfpr = (double) mi.in.get("RFPR")
      if (rfpr <= 0) {
        mi.error("Le prix de référence doit être supérieur à 0")
        return
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
        if (lktp != 3 && ((rfpr / pucd) > ajpupr)) {
          mi.error("Le prix de référence ne doit être supérieur au prix du contrat d'achat' : " + (ajpupr * pucd))
          return
        }
      }
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
        mi.error("Le prix de vente  n'existe pas AGNO : " + agno + " CUNO : " + cuno + " OBV1 : " + uwo1 + " OBV2 : " + uwo2 + " OBV3 : " + uwo3 + " OBV4 : " + uwo4 + " FDAT : " + fdat + " STDT : " + stdt + " ¨PREX : " + prex)
        return
      } else {
        olagpr = (double) oagrprRequest.get("OLAGPR")
        sacd = (double) oagrprRequest.get("OLSACD")
        if (sacd == 0d) {
          sacd = 1
        }
        if (lktp!=3 &&((rfpr / sacd) > olagpr)) {
          mi.error("Le prix de référence ne doit pas être supérieur au prix du contrat de vente : " + (olagpr * sacd))
          return
        }
      }
    }

    //Check if TXID Change
    if (txid != intxid) {

      if (intxid > 0) {
        // Check TXID exist in CSYTXH
        ExpressionFactory csytxhExpression = database.getExpressionFactory("CSYTXH")
        csytxhExpression = csytxhExpression.eq("THFILE", "EXT01000")
        csytxhExpression = csytxhExpression.eq("THKFLD", lkid as String)
        DBAction csytxhQuery = database.table("CSYTXH").matching(csytxhExpression).index("00").build()
        DBContainer csytxhRequest = csytxhQuery.getContainer()
        csytxhRequest.set("THCONO", currentCompany)
        csytxhRequest.set("THDIVI", "")
        csytxhRequest.set("THTXID", intxid)
        csytxhRequest.set("THTXVR", "FIL")
        csytxhRequest.set("THLNCD", "")
        if (!csytxhQuery.read(csytxhRequest)) {
          mi.error("Error : Bloc text " + intxid + " N'existe pas")
          return
        } else {
        }
      }

      if (txid > 0) {
        // Delete old txid
        executeCRS980MIDltTxtBlockLins("", txid as String, "FIL", "", "CSYTXH", "EXT01000")
        if (suppTXID != "") {
          mi.error("Error : " + suppTXID)
          return
        }
      }
      // Check if new TXID Exist

    }

    Closure<?> ext010Updater = { LockedResult ext010LockedResult ->
      if (mi.in.get("LKRF") != null)
        ext010LockedResult.set("EXLKRF", lkrf)
      if (mi.in.get("LKQT") != null)
        ext010LockedResult.setDouble("EXLKQT", lkqt as Double)
      if (mi.in.get("RFPR") != null)
        ext010LockedResult.setDouble("EXRFPR", rfpr/pucd as Double)
      if (mi.in.get("DUDT") != null)
        ext010LockedResult.setInt("EXDUDT", dudt as Integer)
      if (mi.in.get("IVDT") != null)
        ext010LockedResult.setInt("EXIVDT", ivdt as Integer)
      if (mi.in.get("TXID") != null)
        ext010LockedResult.setLong("EXTXID", intxid as Long)
      ext010LockedResult.set("EXLMDT", utility.call("DateUtil", "currentDateY8AsInt"))
      ext010LockedResult.setInt("EXCHNO", ((Integer) ext010LockedResult.get("EXCHNO") + 1))
      ext010LockedResult.set("EXCHID", program.getUser())
      ext010LockedResult.update()
    }
    ext010Query.readLock(ext010Request, ext010Updater)
  }
  /**
   * Execute CRS980 DltTxtBlockLins
   * @param divi
   * @param txid
   * @param txvr
   * @param lncd
   * @param tfil
   * @param file
   */
  private void executeCRS980MIDltTxtBlockLins(String divi, String txid, String txvr, String lncd, String tfil, String file) {
    LinkedHashMap<String, String> parameters = [
      "DIVI": divi,
      "TXID": txid,
      "TXVR": txvr,
      "LNCD": lncd,
      "TFIL": tfil,
      "FILE": file
    ]
    Closure<?> handler = { Map<String, String> response ->

      if (response.error != null) {
        suppTXID = "Failed CRS980MI.DltTxtBlockLins: " + response.errorMessage
      } else {
        suppTXID = ""
      }
    }
    miCaller.call("CRS980MI", "DltTxtBlockLins", parameters, handler)
  }
}
