/****************************************************************************************
 Extension Name: EXT010MI/GetLink
 Type: ExtendM3Transaction
 Script Author: Patrick BEAUDOUIN
 Date: 2024-01-01
 Description:
 * Get link Circles and WashOut

 Revision History:
 Name                   Date             Version          Description of Changes
 PBEAUDOUIN             2024-01-01       1.0              CRO01 Management Circles and WashOut - Create GetLink transaction
 PBEAUDOUIN             2024-04-22       1.1              CRO01 Management Circles and WashOut - Change for approval
 ******************************************************************************************/

import java.time.LocalDateTime

public class GetLink extends ExtendM3Transaction {
  private final MIAPI mi
  private final DatabaseAPI database
  private final LoggerAPI logger
  private final ProgramAPI program
  private final UtilityAPI utility
  private final MICallerAPI miCaller
  private Integer currentCompany
  private Integer lkid
  private double sacd
  private double rfpr

  /*
 * Transaction EXT010MI/GetLink Interface
 * @param mi - Infor MI Interface
 * @param database - Infor Database Interface
 * @param logger - Infor Logging Interface
 * @param Program - Infor program Interface
 * @param utility - utility Interface
 * @param miCaller - Infor miCaller Interface
 */

  public GetLink(MIAPI mi, DatabaseAPI database, LoggerAPI logger, ProgramAPI program, UtilityAPI utility, MICallerAPI miCaller) {
    this.mi = mi
    this.database = database
    this.logger = logger
    this.program = program
    this.miCaller = miCaller
    this.utility = utility
  }

  public void main() {
    lkid = 0
    currentCompany = (Integer) program.getLDAZD().CONO
    if (mi.in.get("LKID") != null) {
      lkid = mi.in.get("LKID")
    } else {
      mi.error("Identifiant lien obligatoire")
      return
    }
    LocalDateTime timeOfCreation = LocalDateTime.now()
    DBAction ext010Query = database.table("EXT010").selection("EXCONO","EXLKID","EXSUNO","EXAGNB","EXGRPI","EXOBV1","EXOBV2","EXOBV3","EXOBV4","EXFVDT","EXUWDI","EXCUNO","EXAGNO","EXFDAT","EXSTDT","EXPREX","EXUWO1","EXUWO2","EXUWO3","EXUWO4","EXLKTP","EXLKRF","EXLKQT","EXRFPR","EXDUDT","EXIVDT","EXSTAT","EXTXID").index("00").build()
    DBContainer ext010Request = ext010Query.getContainer()
    ext010Request.set("EXCONO", currentCompany)
    ext010Request.set("EXLKID", lkid)

    //Record exists
    if (!ext010Query.read(ext010Request)) {
      mi.error("L'enregistrement n'existe pas")
      return
    }

    // Check Price OIS062
    DBAction oagrprQuery = database.table("OAGRPR").selection("OLAGPR","OLSACD").index("00").build()
    DBContainer oagrprRequest = oagrprQuery.getContainer()
    oagrprRequest.set("OLCONO", currentCompany)
    oagrprRequest.set("OLCUNO", ext010Request.get("EXCUNO"))
    oagrprRequest.set("OLAGNO", ext010Request.get("EXAGNO"))
    oagrprRequest.set("OLFDAT", ext010Request.get("EXFDAT"))
    oagrprRequest.set("OLSTDT", ext010Request.get("EXSTDT"))
    oagrprRequest.set("OLPREX", ext010Request.get("EXPREX"))
    oagrprRequest.set("OLOBV1", ext010Request.get("EXUWO1"))
    oagrprRequest.set("OLOBV2", ext010Request.get("EXUWO2"))
    oagrprRequest.set("OLOBV3",  ext010Request.get("EXUWO3"))
    oagrprRequest.set("OLOBV4", ext010Request.get("EXUWO4"))
    if (!oagrprQuery.read(oagrprRequest)) {
      mi.error("Le prix de vente  n'existe pas ")
      return
    }else {
      sacd = (double) oagrprRequest.get("OLSACD")
      if (sacd==0d){
        sacd=1
      }
      rfpr = (double) ext010Request.get("EXRFPR")
      rfpr = rfpr * sacd
    }
    mi.outData.put("CONO", ext010Request.get("EXCONO") as String)
    mi.outData.put("LKID", ext010Request.get("EXLKID")as String)
    mi.outData.put("SUNO",ext010Request.get("EXSUNO")as String)
    mi.outData.put("AGNB", ext010Request.get("EXAGNB")as String)
    mi.outData.put("GRPI",ext010Request.get("EXGRPI")as String)
    mi.outData.put("OBV1",ext010Request.get("EXOBV1")as String)
    mi.outData.put("OBV2",ext010Request.get("EXOBV2")as String)
    mi.outData.put("OBV3",ext010Request.get("EXOBV3")as String)
    mi.outData.put("OBV4",ext010Request.get("EXOBV4")as String)
    mi.outData.put("FVDT",ext010Request.get("EXFVDT")as String)
    mi.outData.put("UWDI",ext010Request.get("EXUWDI")as String)
    mi.outData.put("CUNO", ext010Request.get("EXCUNO")as String)
    mi.outData.put("AGNO", ext010Request.get("EXAGNO")as String)
    mi.outData.put("FDAT", ext010Request.get("EXFDAT")as String)
    mi.outData.put("STDT", ext010Request.get("EXSTDT")as String)
    mi.outData.put("PREX", ext010Request.get("EXPREX")as String)
    mi.outData.put("UWO1", ext010Request.get("EXUWO1")as String)
    mi.outData.put("UWO2", ext010Request.get("EXUWO2")as String)
    mi.outData.put("UWO3", ext010Request.get("EXUWO3")as String)
    mi.outData.put("UWO4", ext010Request.get("EXUWO4")as String)
    mi.outData.put("LKTP", ext010Request.get("EXLKTP")as String)
    mi.outData.put("LKRF", ext010Request.get("EXLKRF")as String)
    mi.outData.put("LKQT", ext010Request.get("EXLKQT")as String)
    mi.outData.put("RFPR", rfpr as String)
    mi.outData.put("DUDT", ext010Request.get("EXDUDT")as String)
    mi.outData.put("IVDT", ext010Request.get("EXIVDT")as String)
    mi.outData.put("STAT", ext010Request.get("EXSTAT")as String)
    mi.outData.put("TXID", ext010Request.get("EXTXID")as String)
    mi.write()
  }
}
