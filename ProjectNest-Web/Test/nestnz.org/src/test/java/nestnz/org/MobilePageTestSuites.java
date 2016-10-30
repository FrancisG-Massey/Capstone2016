package nestnz.org;

import java.util.regex.Pattern;
import java.util.concurrent.TimeUnit;
import org.junit.*;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.Select;

public class MobilePageTestSuites {
	  private WebDriver driver;
	  private String baseUrl;
	  private boolean acceptNextAlert = true;
	  private StringBuffer verificationErrors = new StringBuffer();

	  @Before
	  public void setUp() throws Exception {
	    baseUrl = "https://www.nestnz.org/";
	  }

	 /* @Test
	  public void LaunchTestOnIE() throws Exception {
		System.setProperty("webdriver.edge.driver",".\\Browsers\\MicrosoftWebDriver.exe");
		driver = new EdgeDriver();
		  driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
		  testMobilePageTestSuites();
	  }*/


	  @Test
	  public void LaunchTestOnFireFox() throws Exception {
		  System.setProperty("webdriver.gecko.driver",".\\Browsers\\geckodriver.exe");
	      driver = new FirefoxDriver();
	      driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
	      testMobilePageTestSuites();
	  }
	  @Test
	  public void LaunchTestOnChrome() throws Exception {
		  System.setProperty("webdriver.chrome.driver", ".\\Browsers\\chromedriver.exe");
	      driver = new ChromeDriver();
	      driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
	      testMobilePageTestSuites();
	  }
	  
  private void testMobilePageTestSuites() throws Exception {
    driver.get(baseUrl + "/#/");
    driver.findElement(By.linkText("Mobile App")).click();
    try {
      assertEquals("Mobile Application", driver.findElement(By.cssSelector("b")).getText());
    } catch (Error e) {
      verificationErrors.append(e.toString());
    }
    assertEquals("Download", driver.findElement(By.cssSelector("h3 > b")).getText());
    assertTrue(isElementPresent(By.cssSelector("img[alt=\"Get it on Google Play\"]")));
    assertEquals("", driver.findElement(By.cssSelector("img[alt=\"Get it on Google Play\"]")).getText());
    assertTrue(isElementPresent(By.cssSelector("div.text-center.ng-scope")));
    assertTrue(isElementPresent(By.cssSelector("b.ng-binding")));
    assertEquals("Users will be registered to traplines for which they volunteer, these traplines will be displayed on a selection screen according to their region. The user can select the trapline they are resetting and they will be taken to a screen displaying information on the trapline.", driver.findElement(By.xpath("//div/div/div/div/p")).getText());
    assertTrue(isElementPresent(By.cssSelector("img.img-mobile.pull-left")));
    assertEquals("Map", driver.findElement(By.xpath("//div[2]/div/div/h3/b")).getText());
    assertEquals("Use the inbuilt map system to locate traps. Maps can be pre-loaded so that all the features are able to be used when in the bush and without internet access. Vibration and/or sound will alert the user to their proximity to the next trap facilitating limited interaction with the mobile device while in the bush.", driver.findElement(By.xpath("//div[2]/div/div/p")).getText());
    assertTrue(isElementPresent(By.cssSelector("img.img-mobile.pull-right")));
    assertEquals("Select Navigation", driver.findElement(By.xpath("//div[3]/div/div/h3/b")).getText());
    assertEquals("The user can select a certain range of traps to reset on a particular trapline. This feature also facilitates visiting traps in reverse order.", driver.findElement(By.xpath("//div[3]/div/div/p")).getText());
    assertTrue(isElementPresent(By.xpath("//div[3]/div/div/img")));
    assertEquals("Log Catch", driver.findElement(By.xpath("//div[4]/div/div/h3/b")).getText());
    assertEquals("When the user resets a trap, log the animal that has been caught through the 'trap catch' screen. This screen features large buttons which can be easily pressed while wearing gloves.", driver.findElement(By.xpath("//div[4]/div/div/p")).getText());
    assertTrue(isElementPresent(By.xpath("//div[4]/div/div/img")));
  }

  @After
  public void tearDown() throws Exception {
    driver.quit();
    String verificationErrorString = verificationErrors.toString();
    if (!"".equals(verificationErrorString)) {
      fail(verificationErrorString);
    }
  }

  private boolean isElementPresent(By by) {
    try {
      driver.findElement(by);
      return true;
    } catch (NoSuchElementException e) {
      return false;
    }
  }

  private boolean isAlertPresent() {
    try {
      driver.switchTo().alert();
      return true;
    } catch (NoAlertPresentException e) {
      return false;
    }
  }

  private String closeAlertAndGetItsText() {
    try {
      Alert alert = driver.switchTo().alert();
      String alertText = alert.getText();
      if (acceptNextAlert) {
        alert.accept();
      } else {
        alert.dismiss();
      }
      return alertText;
    } finally {
      acceptNextAlert = true;
    }
  }
}
