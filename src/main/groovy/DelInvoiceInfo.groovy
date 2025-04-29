/****************************************************************************************
 Extension Name: EXT020MI/DelInvoiceInfo
 Type: ExtendM3Transaction
 Script Author: HPILON
 Date: 202504018
 Description:
 * FIN11 - Easy Invoice integration
 * Delete Invoice info from EasyInvoice

 Revision History:
 Name                    Date             Version          Description of Changes
 HPILON                  2025-04-18       1.0              First creation
 ******************************************************************************************/

public class DelInvoiceInfo extends ExtendM3Transaction {
  private final MIAPI mi
  private final DatabaseAPI database
  private final LoggerAPI logger
  private final ProgramAPI program
  private final MICallerAPI miCaller
  private Integer currentCompany
  private String currentDivision

  /*
  * Transaction EXT020MI/DelInvoiceInfo Interface
  * @param mi - Infor MI Interface
  * @param database - Infor Database Interface
  * @param logger - Infor Logging Interface
  * @param Program - Infor program Interface
   * @param miCaller - Infor miCaller Interface
  */

  public DelInvoiceInfo(MIAPI mi, DatabaseAPI database, ProgramAPI program, LoggerAPI logger, MICallerAPI miCaller) {
    this.mi = mi
    this.database = database
    this.logger = logger
    this.program = program
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

    DBAction query = database.table("EXT020").index("00").build()
    DBContainer EXT020 = query.getContainer()
    EXT020.set("EXCONO", currentCompany)
    EXT020.set("EXDIVI", currentDivision)
    EXT020.set("EXINYR", mi.in.get("INYR"))
    EXT020.set("EXSUNO", mi.in.get("SUNO"))
    EXT020.set("EXSINO", mi.in.get("SINO"))
    EXT020.set("EXZTYP", mi.in.get("ZTYP"))

    //Record exists
    if (!query.readLock(EXT020, updateCallBack)) {
      mi.error("L'enregistrement n'existe pas")
      return
    }
  }

  Closure<?> updateCallBack = { LockedResult lockedResult ->
    lockedResult.delete()
  }

}
