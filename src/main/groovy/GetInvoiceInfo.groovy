/****************************************************************************************
 Extension Name: EXT020MI/GetInvoiceInfo
 Type: ExtendM3Transaction
 Script Author: HPILON
 Date: 20250409
 Description:
 * FIN11 - Easy Invoice integration
 * Get invoice info from EXT020

 Revision History:
 Name                    Date             Version          Description of Changes
 HPILON                  2025-04-09       1.0              First creation
 ******************************************************************************************/

public class GetInvoiceInfo extends ExtendM3Transaction {
  private final MIAPI mi
  private final LoggerAPI logger
  private final ProgramAPI program
  private final DatabaseAPI database
  private final MICallerAPI miCaller
  private Integer currentCompany
  private String currentDivision

  /*
  * Transaction EXT020MI/GetInvoiceInfo Interface
  * @param mi - Infor MI Interface
  * @param database - Infor Database Interface
  * @param Program - Infor program Interface
  * @param logger - Infor Logging Interface
  * @param miCaller - Infor miCaller Interface
  */

  public GetInvoiceInfo(MIAPI mi, DatabaseAPI database, ProgramAPI program, LoggerAPI logger, MICallerAPI miCaller) {
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

    if (!query.read(EXT020)) {
      mi.error("L'enregistrement n'existe pas")
      return
    }
    else {
      mi.outData.put("CONO", EXT020.get("EXCONO") as String)
      mi.outData.put("DIVI", EXT020.get("EXDIVI") as String)
      mi.outData.put("INYR", EXT020.get("EXINYR") as String)
      mi.outData.put("SUNO", EXT020.get("EXSUNO") as String)
      mi.outData.put("SINO", EXT020.get("EXSINO") as String)
      mi.outData.put("ZTYP", EXT020.get("EXZTYP") as String)
      mi.outData.put("YEA4", EXT020.get("EXYEA4") as String)
      mi.outData.put("VSER", EXT020.get("EXVSER") as String)
      mi.outData.put("VONO", EXT020.get("EXVONO") as String)
      mi.outData.put("IVDT", EXT020.get("EXIVDT") as String)
      mi.outData.put("ACDT", EXT020.get("EXACDT") as String)
      mi.outData.put("CUCD", EXT020.get("EXCUCD") as String)
      mi.outData.put("AIT1", EXT020.get("EXAIT1") as String)
      mi.outData.put("AIT2", EXT020.get("EXAIT2") as String)
      mi.outData.put("AIT3", EXT020.get("EXAIT3") as String)
      mi.outData.put("AIT4", EXT020.get("EXAIT4") as String)
      mi.outData.put("AIT5", EXT020.get("EXAIT5") as String)
      mi.outData.put("AIT6", EXT020.get("EXAIT6") as String)
      mi.outData.put("AIT7", EXT020.get("EXAIT7") as String)
      mi.outData.put("AMNT", EXT020.get("EXAMNT") as String)
      mi.outData.put("RGDT", EXT020.get("EXRGDT") as String)
      mi.outData.put("RGTM", EXT020.get("EXRGTM") as String)
      mi.outData.put("LMDT", EXT020.get("EXLMDT") as String)
      mi.outData.put("CHNO", EXT020.get("EXCHNO") as String)
      mi.outData.put("CHID", EXT020.get("EXCHID") as String)
      mi.write()
    }

  }
}
