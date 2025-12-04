import static com.kms.katalon.core.checkpoint.CheckpointFactory.findCheckpoint
import static com.kms.katalon.core.testcase.TestCaseFactory.findTestCase
import static com.kms.katalon.core.testdata.TestDataFactory.findTestData
import static com.kms.katalon.core.testobject.ObjectRepository.findTestObject
import static com.kms.katalon.core.testobject.ObjectRepository.findWindowsObject
import com.kms.katalon.core.checkpoint.Checkpoint as Checkpoint
import com.kms.katalon.core.cucumber.keyword.CucumberBuiltinKeywords as CucumberKW
import com.kms.katalon.core.mobile.keyword.MobileBuiltInKeywords as Mobile
import com.kms.katalon.core.model.FailureHandling as FailureHandling
import com.kms.katalon.core.testcase.TestCase as TestCase
import com.kms.katalon.core.testdata.TestData as TestData
import com.kms.katalon.core.testng.keyword.TestNGBuiltinKeywords as TestNGKW
import com.kms.katalon.core.testobject.TestObject as TestObject
import com.kms.katalon.core.webservice.keyword.WSBuiltInKeywords as WS
import com.kms.katalon.core.webui.keyword.WebUiBuiltInKeywords as WebUI
import com.kms.katalon.core.windows.keyword.WindowsBuiltinKeywords as Windows
import internal.GlobalVariable as GlobalVariable
import org.openqa.selenium.Keys as Keys

Mobile.startApplication('C:\\Users\\User\\AndroidStudioProjects\\TAES\\code\\BiscaTAES\\app\\build\\outputs\\apk\\debug\\app-debug.apk', 
    true)

Mobile.tap(findTestObject('Object Repository/android.widget.Button - Jogar Anonimo'), 0)

Mobile.tapAndHold(findTestObject('android.widget.TextView - Welcome'), 2, 0)

Mobile.tap(findTestObject('Object Repository/android.widget.Button - Dev Debug Deal'), 0)

Mobile.tap(findTestObject('Object Repository/android.widget.TextView - 2'), 0)

WebUI.delay(2)

Mobile.tap(findTestObject('Object Repository/android.widget.TextView - 3'), 0)

WebUI.delay(2)

Mobile.tap(findTestObject('Object Repository/android.widget.TextView - 4'), 0)

WebUI.delay(2)

Mobile.tap(findTestObject('Object Repository/android.widget.TextView - 5'), 0)

WebUI.delay(2)

Mobile.tap(findTestObject('Object Repository/android.widget.TextView - 6'), 0)

WebUI.delay(2)

Mobile.tap(findTestObject('Object Repository/android.widget.TextView - Q'), 0)

WebUI.delay(2)

Mobile.tap(findTestObject('Object Repository/android.widget.TextView - J'), 0)

WebUI.delay(2)

Mobile.tap(findTestObject('Object Repository/android.widget.TextView - K'), 0)

WebUI.delay(2)

Mobile.tap(findTestObject('Object Repository/android.widget.TextView - 7'), 0)

WebUI.delay(2)

Mobile.tap(findTestObject('Object Repository/android.widget.TextView - A'), 0)

WebUI.delay(2)

Mobile.tap(findTestObject('Object Repository/android.widget.TextView - 2 (1)'), 0)

WebUI.delay(2)

Mobile.tap(findTestObject('Object Repository/android.widget.TextView - 3 (1)'), 0)

WebUI.delay(2)

Mobile.tap(findTestObject('Object Repository/android.widget.TextView - 4 (1)'), 0)

WebUI.delay(2)

Mobile.tap(findTestObject('Object Repository/android.widget.TextView - 5 (1)'), 0)

WebUI.delay(2)

Mobile.tap(findTestObject('Object Repository/android.widget.TextView - 6 (1)'), 0)

WebUI.delay(2)

Mobile.tap(findTestObject('Object Repository/android.widget.TextView - Q (1)'), 0)

WebUI.delay(2)

Mobile.tap(findTestObject('Object Repository/android.widget.TextView - J (1)'), 0)

WebUI.delay(2)

Mobile.tap(findTestObject('Object Repository/android.widget.TextView - K (1)'), 0)

WebUI.delay(2)

Mobile.tap(findTestObject('Object Repository/android.widget.TextView - 7 (1)'), 0)

WebUI.delay(2)

Mobile.tapAtPosition(84, 2228)

WebUI.delay(5)

Mobile.tap(findTestObject('Object Repository/android.widget.Button - Voltar ao Menu'), 0)

Mobile.closeApplication()

