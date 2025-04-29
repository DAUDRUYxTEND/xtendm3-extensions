/****************************************************************************************
 Extension Name: EXT020MI/AddInvoiceInfo
 Type: ExtendM3Transaction
 Script Author: HPILON
 Date: 20250409
 Description:
 * FIN11 - Easy Invoice integration
 * Add Invoice info from EasyInvoice

 Revision History:
 Name                    Date             Version          Description of Changes
 HPILON                  2025-04-09       1.0              First creation
 ******************************************************************************************/

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

public class AddInvoiceInfo extends ExtendM3Transaction {
  private final MIAPI mi
  private final LoggerAPI logger
  private final ProgramAPI program
  private final DatabaseAPI database
  private final MICallerAPI miCaller
  private Integer currentCompany
  private String currentDivision

  /*
* Transaction EXT020MI/AddInvoiceInfo Interface
* @param mi - Infor MI Interface
* @param database - Infor Database Interface
* @param Program - Infor program Interface
* @param logger - Infor Logging Interface
* @param miCaller - Infor miCaller Interface
*/

  public AddInvoiceInfo(MIAPI mi, DatabaseAPI database, ProgramAPI program, LoggerAPI logger, MICallerAPI miCaller) {
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

    String ait1 = ""
    String ait2 = ""
    String ait3 = ""
    String ait4 = ""
    String ait5 = ""
    String ait6 = ""
    String ait7 = ""

    LocalDateTime timeOfCreation = LocalDateTime.now()
    DBAction query = database.table("EXT020").index("00").build()
    DBContainer EXT020 = query.getContainer()
    EXT020.set("EXCONO", currentCompany)
    EXT020.set("EXDIVI",  currentDivision)
    EXT020.set("EXINYR",  mi.in.get("INYR"))
    EXT020.set("EXSUNO",  mi.in.get("SUNO"))
    EXT020.set("EXSINO",  mi.in.get("SINO"))
    EXT020.set("EXZTYP",  mi.in.get("ZTYP"))

    //if not exist
    if (!query.read(EXT020)) {
      if (mi.in.get("YEA4") != null) {
        EXT020.set("EXYEA4", mi.in.get("YEA4"))
      }

      if (mi.in.get("VSER") != null) {
        EXT020.set("EXVSER", mi.in.get("VSER"))
      }

      if (mi.in.get("VONO") != null) {
        EXT020.set("EXVONO", mi.in.get("VONO"))
      }

      if (mi.in.get("IVDT") != null) {
        EXT020.set("EXIVDT", mi.in.get("IVDT"))
      }
      else {
        mi.error("La date facture est obligatoire")
        return
      }

      if (mi.in.get("ACDT") != null) {
        EXT020.set("EXACDT", mi.in.get("ACDT"))
      }
      else {
        mi.error("La date comptable est obligatoire")
        return
      }


      if (mi.in.get("CUCD") != null) {
        EXT020.set("EXCUCD", mi.in.get("CUCD"))
      }
      else {
        mi.error("La devise est obligatoire")
        return
      }

      if (mi.in.get("AIT1") != null) {
        ait1 = mi.in.get("AIT1")
      }
      if (mi.in.get("AIT2") != null) {
        ait2 = mi.in.get("AIT2")
      }
      if (mi.in.get("AIT3") != null) {
        ait3 = mi.in.get("AIT3")
      }
      if (mi.in.get("AIT4") != null) {
        ait4 = mi.in.get("AIT4")
      }
      if (mi.in.get("AIT5") != null) {
        ait5 = mi.in.get("AIT5")
      }
      if (mi.in.get("AIT6") != null) {
        ait6 = mi.in.get("AIT6")
      }
      if (mi.in.get("AIT7") != null) {
        ait7 = mi.in.get("AIT7")
      }

      //test sur la présence d'au moins un segment renseigné
      String concatSegment = ait1+ait2+ait3+ait4+ait5+ait6+ait7
      if (concatSegment == "") {
        mi.error("Au moins un segment comptable doit être renseigné")
        return
      }

      EXT020.set("EXAIT1", ait1)
      EXT020.set("EXAIT2", ait2)
      EXT020.set("EXAIT3", ait3)
      EXT020.set("EXAIT4", ait4)
      EXT020.set("EXAIT5", ait5)
      EXT020.set("EXAIT6", ait6)
      EXT020.set("EXAIT7", ait7)

      if (mi.in.get("AMNT") != null) {
        EXT020.set("EXAMNT", mi.in.get("AMNT"))
      }
      else {
        mi.error("Le montant facture est obligatoire")
        return
      }
      EXT020.setInt("EXRGDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer)
      EXT020.setInt("EXRGTM", timeOfCreation.format(DateTimeFormatter.ofPattern("HHmmss")) as Integer)
      EXT020.setInt("EXLMDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer)
      EXT020.setInt("EXCHNO", 1)
      EXT020.set("EXCHID", program.getUser())
      query.insert(EXT020)
    } else {
      mi.error("L'enregistrement existe déjà")
      return
    }
  }
}
