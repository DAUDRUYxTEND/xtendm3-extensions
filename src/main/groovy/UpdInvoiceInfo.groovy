/****************************************************************************************
 Extension Name: EXT020MI/GetInvoiceInfo
 Type: ExtendM3Transaction
 Script Author: HPILON
 Date: 20250409
 Description:
 * FIN11 - Easy Invoice integration
 * Update invoice info in EXT020

 Revision History:
 Name                    Date             Version          Description of Changes
 HPILON                  2025-04-09       1.0              First creation
 ******************************************************************************************/

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

public class UpdInvoiceInfo extends ExtendM3Transaction {
  private final MIAPI mi
  private final LoggerAPI logger
  private final ProgramAPI program
  private final DatabaseAPI database
  private final MICallerAPI miCaller
  private Integer currentCompany
  private String currentDivision

  /*
* Transaction  EXT020MI/GetInvoiceInfo Interface
* @param mi - Infor MI Interface
* @param database - Infor Database Interface
* @param Program - Infor program Interface
* @param logger - Infor Logging Interface
* @param miCaller - Infor miCaller Interface
*/

  public UpdInvoiceInfo(MIAPI mi, DatabaseAPI database, ProgramAPI program, LoggerAPI logger, MICallerAPI miCaller) {
    this.mi = mi
    this.database = database
    this.program = program
    this.logger = logger
    this.miCaller = miCaller
  }
  public void main() {
    if (mi.in.get("CONO") == null) {
      currentCompany = (Integer)program.getLDAZD().CONO
    } else {
      currentCompany = mi.in.get("CONO")
    }

    if (mi.in.get("DIVI") == null) {
      currentDivision = (String)program.getLDAZD().DIVI
    } else {
      currentDivision = mi.in.get("DIVI")
    }

    DBAction query = database.table("EXT020").index("00").selection("EXCONO","EXDIVI","EXINYR","EXSUNO","EXSINO","EXZTYP","EXYEA4","EXVSER","EXVONO","EXIVDT","EXACDT","EXCUCD","EXAIT1","EXAIT2","EXAIT3","EXAIT4","EXAIT5","EXAIT6","EXAIT7","EXAMNT","EXRGDT","EXRGTM","EXLMDT","EXCHNO","EXCHID").build()
    DBContainer EXT020 = query.getContainer()
    EXT020.set("EXCONO", currentCompany)
    EXT020.set("EXDIVI", currentDivision)
    EXT020.set("EXINYR", mi.in.get("INYR"))
    EXT020.set("EXSUNO", mi.in.get("SUNO"))
    EXT020.set("EXSINO", mi.in.get("SINO"))
    EXT020.set("EXZTYP", mi.in.get("ZTYP"))
    if (!query.readLock(EXT020, updateCallBack)) {
      mi.error("L'enregistrement n'existe pas")
      return
    }

  }

  Closure<?> updateCallBack = { LockedResult lockedResult ->
    LocalDateTime timeOfCreation = LocalDateTime.now()
    int changeNumber = lockedResult.get("EXCHNO")

    if (mi.in.get("YEA4") != null) {
      lockedResult.set("EXYEA4", mi.in.get("YEA4"))
    }

    if (mi.in.get("VSER") != null) {
      lockedResult.set("EXVSER", mi.in.get("VSER"))
    }

    if (mi.in.get("VONO") != null) {
      lockedResult.set("EXVONO", mi.in.get("VONO"))
    }

    if (mi.in.get("IVDT") != null) {
      lockedResult.set("EXIVDT", mi.in.get("IVDT"))
    }

    if (mi.in.get("ACDT") != null) {
      lockedResult.set("EXACDT", mi.in.get("ACDT"))
    }

    if (mi.in.get("CUCD") != null) {
      lockedResult.set("EXCUCD", mi.in.get("CUCD"))
    }

    if (mi.in.get("AIT1") != null) {
      lockedResult.set("EXAIT1", mi.in.get("AIT1"))
    }

    if (mi.in.get("AIT2") != null) {
      lockedResult.set("EXAIT2", mi.in.get("AIT2"))
    }

    if (mi.in.get("AIT3") != null) {
      lockedResult.set("EXAIT3", mi.in.get("AIT3"))
    }

    if (mi.in.get("AIT4") != null) {
      lockedResult.set("EXAIT4", mi.in.get("AIT4"))
    }

    if (mi.in.get("AIT5") != null) {
      lockedResult.set("EXAIT5", mi.in.get("AIT5"))
    }

    if (mi.in.get("AIT6") != null) {
      lockedResult.set("EXAIT6", mi.in.get("AIT6"))
    }

    if (mi.in.get("AIT7") != null) {
      lockedResult.set("EXAIT7", mi.in.get("AIT7"))
    }

    if (mi.in.get("AMNT") != null) {
      lockedResult.set("EXAMNT", mi.in.get("AMNT"))
    }


    lockedResult.setInt("EXLMDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer)
    lockedResult.setInt("EXCHNO", changeNumber + 1)
    lockedResult.set("EXCHID", program.getUser())
    lockedResult.update()
  }
}
