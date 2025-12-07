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

Mobile.tap(findTestObject('Object Repository/android.widget.Button - Jogar Annimo'), 0)

Mobile.tapAndHold(findTestObject('Object Repository/android.widget.TextView - Welcome'), 2, 0)

Mobile.tap(findTestObject('Object Repository/android.widget.Button - Dev No Shuffle'), 0)

Mobile.tap(findTestObject('android.widget.TextView - 7 Hearts'), 0)

WebUI.delay(2)

Mobile.tap(findTestObject('android.widget.TextView - A Hearts'), 0)

WebUI.delay(2)

Mobile.tap(findTestObject('Object Repository/android.widget.TextView - 7 Diamonds'), 0)

WebUI.delay(2)

Mobile.tap(findTestObject('Object Repository/android.widget.TextView - A Diamonds'), 0)

WebUI.delay(2)

Mobile.tap(findTestObject('Object Repository/android.widget.TextView - 7 Clubs'), 0)

WebUI.delay(2)

Mobile.tap(findTestObject('Object Repository/android.widget.TextView - A Clubs'), 0)

WebUI.delay(2)

Mobile.tap(findTestObject('android.widget.TextView - 7 Spades'), 0)

WebUI.delay(2)

Mobile.tap(findTestObject('android.widget.TextView - A Spades'), 0)

WebUI.delay(2)

Mobile.tap(findTestObject('Object Repository/android.widget.TextView - 2 Hearts'), 0)

WebUI.delay(3)

Mobile.tap(findTestObject('android.widget.TextView - 4 Diamonds'), 0)

WebUI.delay(3)

Mobile.tap(findTestObject('Object Repository/android.widget.TextView - 6 Diamonds'), 0)

WebUI.delay(3)

Mobile.tap(findTestObject('android.widget.TextView - Q Spades'), 0)

WebUI.delay(3)

Mobile.tap(findTestObject('Object Repository/android.widget.TextView - J Diamonds'), 0)

WebUI.delay(3)

Mobile.tap(findTestObject('Object Repository/android.widget.TextView - 2 Clubs'), 0)

WebUI.delay(3)

Mobile.tap(findTestObject('Object Repository/android.widget.TextView - 4 Clubs'), 0)

WebUI.delay(3)

Mobile.tap(findTestObject('android.widget.TextView - 6 Clubs'), 0)

WebUI.delay(3)

Mobile.tap(findTestObject('Object Repository/android.widget.TextView - J Clubs'), 0)

WebUI.delay(3)

Mobile.tap(findTestObject('android.widget.TextView - 2 Spades'), 0)

WebUI.delay(3)

Mobile.tap(findTestObject('Object Repository/android.widget.TextView - 5 Spades'), 0)

WebUI.delay(3)

Mobile.tap(findTestObject('Object Repository/android.widget.TextView - K Spades'), 0)

WebUI.delay(5)

Mobile.tap(findTestObject('android.widget.Button - Proximo Jogo'), 0)

WebUI.delay(2)

Mobile.closeApplication()

