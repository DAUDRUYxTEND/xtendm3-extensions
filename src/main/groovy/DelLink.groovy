/****************************************************************************************
 Extension Name: EXT010MI/DelLink
 Type: ExtendM3Transaction
 Script Author: Patrick BEAUDOUIN
 Date: 2024-01-01
 Description:
 * Delete link Circles and WashOut

 Revision History:
 Name                   Date             Version          Description of Changes
 PBEAUDOUIN             2024-01-01       1.0              CRO01 Management Circles and WashOut - Create DelLInk transaction
 PBEAUDOUIN             2024-04-22       1.1              CRO01 Management Circles and WashOut - Change for approval
 ******************************************************************************************/

import java.time.LocalDateTime

public class DelLink extends ExtendM3Transaction {
  private final MIAPI mi
  private final DatabaseAPI database
  private final LoggerAPI logger
  private final ProgramAPI program
  private final UtilityAPI utility
  private final MICallerAPI miCaller
  private Integer currentCompany
  private Integer lkid
  private suppTXID

  /*
   * Transaction EXT010MI/DelLink Interface
   * @param mi - Infor MI Interface
   * @param database - Infor Database Interface
   * @param logger - Infor Logging Interface
   * @param Program - Infor program Interface
   * @param utility - utility Interface
   * @param miCaller - Infor miCaller Interface
   */

  public DelLink(MIAPI mi, DatabaseAPI database, LoggerAPI logger, ProgramAPI program, UtilityAPI utility, MICallerAPI miCaller) {
    this.mi = mi
    this.database = database
    this.logger = logger
    this.program = program
    this.miCaller = miCaller
    this.utility = utility
  }

  public void main() {
    lkid = 0
    int stat = 0
    int txid = 0
    suppTXID =""
    currentCompany = (Integer) program.getLDAZD().CONO

    if (mi.in.get("LKID") != null) {
      lkid = mi.in.get("LKID")
    } else {
      mi.error("Identifiant lien obligatoire")
      return
    }

    //Check supplier exist
    LocalDateTime timeOfCreation = LocalDateTime.now()
    DBAction ext010Query = database.table("EXT010").selection("EXSTAT","EXTXID").index("00").build()
    DBContainer ext010Request = ext010Query.getContainer()
    ext010Request.set("EXCONO", currentCompany)
    ext010Request.set("EXLKID", lkid)

    //Record exists
    if (!ext010Query.read(ext010Request)) {
      mi.error("L'enregistrement n'existe pas")
      return
    } else {
      // Check Status <20
      stat = ext010Request.get("EXSTAT")
      txid = ext010Request.get("EXTXID")
      if (stat >= 20) {
        mi.error("Statut : " + stat + " suppression interdite")
        return
      }

      if (txid > 0) {
        // Check TXID exist in CSYTXH
        ExpressionFactory csytxhExpression = database.getExpressionFactory("CSYTXH")
        csytxhExpression = csytxhExpression.eq("THFILE", "EXT01000")
        csytxhExpression = csytxhExpression.eq("THKFLD", lkid as String)
        DBAction csytxhQuery = database.table("CSYTXH").matching(csytxhExpression).index("00").build()
        DBContainer csytxhRequest = csytxhQuery.getContainer()
        csytxhRequest.set("THCONO", currentCompany)
        csytxhRequest.set("THDIVI", "")
        csytxhRequest.set("THTXID", txid)
        csytxhRequest.set("THTXVR", "FIL")
        csytxhRequest.set("THLNCD", "")
        if (csytxhQuery.read(csytxhRequest)) {
          executeCRS980MIDltTxtBlockLins("",txid as String,"FIL","","CSYTXH","EXT01000")
          if (suppTXID != ""){
            mi.error("Error : "+suppTXID)
            return
          }
        } else {
        }
      }

      Closure<?> ext010Updater = { LockedResult ext010LockedResult ->
        ext010LockedResult.delete()
      }
      ext010Query.readLock(ext010Request, ext010Updater)
    }
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
  private void executeCRS980MIDltTxtBlockLins(String divi, String txid , String txvr, String lncd, String tfil , String file) {
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

