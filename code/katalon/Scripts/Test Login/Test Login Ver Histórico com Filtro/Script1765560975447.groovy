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

Mobile.tap(findTestObject('android.widget.Button - Login'), 0)

Mobile.setText(findTestObject('android.widget.EditText - Email'), 'aluno@mail.com', 0)

Mobile.setText(findTestObject('android.widget.EditText - Password'), '123', 0)

Mobile.tap(findTestObject('android.widget.Button - Entrar'), 0)

Mobile.tap(findTestObject('android.widget.Button - Historico de Partidas'), 0)

Mobile.tap(findTestObject('Object Repository/android.widget.Spinner'), 0)

Mobile.tap(findTestObject('Object Repository/android.widget.CheckedTextView - Vitrias'), 0)

Mobile.tap(findTestObject('Object Repository/android.widget.Button - Aplicar'), 0)

Mobile.tap(findTestObject('Object Repository/android.widget.Spinner'), 0)

Mobile.tap(findTestObject('Object Repository/android.widget.CheckedTextView - Derrotas'), 0)

Mobile.tap(findTestObject('Object Repository/android.widget.Button - Aplicar'), 0)

Mobile.tap(findTestObject('Object Repository/android.widget.Spinner'), 0)

Mobile.tap(findTestObject('Object Repository/android.widget.CheckedTextView - Todos'), 0)

Mobile.tap(findTestObject('Object Repository/android.widget.Button - Aplicar'), 0)

Mobile.tap(findTestObject('android.widget.Spinner (1)'), 0)

Mobile.tap(findTestObject('Object Repository/android.widget.CheckedTextView - Data (Mais Recente)'), 0)

Mobile.tap(findTestObject('Object Repository/android.widget.Button - Aplicar'), 0)

Mobile.tap(findTestObject('android.widget.Spinner (1)'), 0)

Mobile.tap(findTestObject('Object Repository/android.widget.CheckedTextView - Data (Mais Antigo)'), 0)

Mobile.tap(findTestObject('Object Repository/android.widget.Button - Aplicar'), 0)

Mobile.tap(findTestObject('android.widget.Spinner (1)'), 0)

Mobile.tap(findTestObject('Object Repository/android.widget.CheckedTextView - Duracao (Menor)'), 0)

Mobile.tap(findTestObject('Object Repository/android.widget.Button - Aplicar'), 0)

Mobile.tap(findTestObject('android.widget.Spinner (1)'), 0)

Mobile.tap(findTestObject('Object Repository/android.widget.CheckedTextView - Duracao (Maior)'), 0)

Mobile.tap(findTestObject('Object Repository/android.widget.Button - Aplicar'), 0)

Mobile.tap(findTestObject('android.widget.Spinner (1)'), 0)

Mobile.tap(findTestObject('android.widget.CheckedTextView - Data (Mais Recente)'), 0)

Mobile.tap(findTestObject('Object Repository/android.widget.Button - Aplicar'), 0)

Mobile.closeApplication()

