package nestnz.org;


import java.util.regex.Pattern;
import java.util.concurrent.TimeUnit;
import org.junit.*;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.Select;

public class AboutPageTestSuites {
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
		  testAboutPageTestSuites();
	  }*/

	  @Test
	  public void LaunchTestOnFireFox() throws Exception {
		  System.setProperty("webdriver.gecko.driver",".\\Browsers\\geckodriver.exe");
	      driver = new FirefoxDriver();
	      driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
	      testAboutPageTestSuites();
	  }
	  @Test
	  public void LaunchTestOnChrome() throws Exception {
		  System.setProperty("webdriver.chrome.driver", ".\\Browsers\\chromedriver.exe");
	      driver = new ChromeDriver();
	      driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
	      testAboutPageTestSuites();
	  }

  private void testAboutPageTestSuites() throws Exception {
	driver.get("https://www.nestnz.org");
    driver.findElement(By.linkText("About")).click();
    assertEquals("About", driver.findElement(By.cssSelector("b")).getText());
    assertTrue(isElementPresent(By.cssSelector("b")));
    assertTrue(isElementPresent(By.cssSelector("div.jumbotron.ng-scope")));
    assertTrue(isElementPresent(By.xpath("//div/img")));
    assertTrue(isElementPresent(By.xpath("//div[2]/div/div/img")));
    assertTrue(isElementPresent(By.xpath("//div[3]/div/div/img")));
    assertEquals("Contribute", driver.findElement(By.cssSelector("b.ng-binding")).getText());
    assertEquals("Contribute to New Zealand's goal to become predator free by 2050 by volunteering with your local branch of the Department of Conservation. You could help to reset predator traplines in the New Zealand bush and use the Nest NZ mobile application to record the predators caught.", driver.findElement(By.xpath("//p")).getText());
    assertEquals("Connect", driver.findElement(By.xpath("//div[2]/div/div/h3/b")).getText());
    assertEquals("Connect with Project Nest NZ by contacting the core development team or following us on Twitter (@NestNZ) or Facebook.", driver.findElement(By.xpath("//div[2]/div/div/p")).getText());
    assertEquals("Collaborate", driver.findElement(By.xpath("//div[3]/div/div/h3/b")).getText());
    assertEquals("Project Nest NZ is an open source project! Collaboration in any area - UX design, web design, mobile development etc. - is welcome. Please read through the project wiki on the public Github repository, or contact the core development team, to get started!", driver.findElement(By.xpath("//div[3]/div/div/p")).getText());
  }

  @After
  public void tearDown() throws Exception {
	  driver.quit();
	    driver = null;
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
