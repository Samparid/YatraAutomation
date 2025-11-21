package com.yatra.automation;

import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

public class YatraAutomationScript {

    public static void main(String[] args) throws InterruptedException {

        ChromeOptions chromeOptions = new ChromeOptions();
        chromeOptions.addArguments("--disable-notifications");
        WebDriver driver = new ChromeDriver(chromeOptions);
        WebDriverWait webDriverWait = new WebDriverWait(driver, Duration.ofSeconds(20));
        driver.get("https://www.yatra.com");
        driver.manage().window().maximize();
        HandleRegistrationPopUps(webDriverWait);
        By departureDateButtonLocator = By.xpath("//div[@aria-label='Departure Date inputbox'and @role='button']");
        WebElement departureDateButton = webDriverWait.until(ExpectedConditions.elementToBeClickable(departureDateButtonLocator));
        assert departureDateButton != null;
        departureDateButton.click();
        WebElement NovCalendarWebElement = selectTheMonthFromCalendar(0, webDriverWait);//current month
        WebElement DecCalendarWebElement = selectTheMonthFromCalendar(1, webDriverWait);//Next Month
        Thread.sleep(2000);//current month
        String lowestPriceForCurrentMonth = getMeLowestPrice(NovCalendarWebElement);
        String lowestPriceForNextMonth = getMeLowestPrice(DecCalendarWebElement);
        double MinimumPrice = compareTwoMonthsPrices(lowestPriceForCurrentMonth, lowestPriceForNextMonth);
        System.out.println("lowest Price for both month is " + MinimumPrice);
        driver.quit();
    }

    private static void HandleRegistrationPopUps(WebDriverWait webDriverWait) {
        By popUpsLocator = By.xpath("//div[contains(@class,\"style_popup\")][1]");
        try {
            WebElement PopElement = webDriverWait.until(ExpectedConditions.visibilityOfElementLocated(popUpsLocator));
            assert PopElement != null;
            WebElement PopUpButton = PopElement.findElement(By.xpath(".//img[@alt=\"cross\"]"));
            PopUpButton.click();
        } catch (TimeoutException e) {
            System.out.println("Pop up not shown on the screen!!");
        }
    }

    private static String getMeLowestPrice(WebElement monthWebElement) {
        By NovPriceLocator = By.xpath(".//span[contains(@class,\"custom-day-content\")]");
        List<WebElement> NovPriceList = monthWebElement.findElements(NovPriceLocator);
        int lowestPrice = Integer.MAX_VALUE;
        WebElement priceElement = null;
        for (var price : NovPriceList) {

            String PriceString = price.getText();
            if (!PriceString.isEmpty()) {
                PriceString = PriceString.replace("₹", "").replace(",", "");
                int priceInt = Integer.parseInt(PriceString);
                if (priceInt < lowestPrice) {
                    lowestPrice = priceInt;
                    priceElement = price;

                }
            }
        }
        assert priceElement != null;
        WebElement dateElement = priceElement.findElement(By.xpath(".//../.."));
        return dateElement.getAttribute("aria-label") + " --- Price is Rs " + lowestPrice;
    }

    public static WebElement selectTheMonthFromCalendar(int index, WebDriverWait webDriverWait) {
        By calendarMonthLocator = By.xpath("//div[@class='react-datepicker__month-container']");
        List<WebElement> CalendarMonthList = webDriverWait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(calendarMonthLocator));
        assert CalendarMonthList != null;
        return CalendarMonthList.get(index);
    }

    public static double compareTwoMonthsPrices(String currentMonthPrice, String nextMonthPrice) {
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("Rs\\s*(\\d+)");
        java.util.regex.Matcher matcher1 = pattern.matcher(currentMonthPrice);
        java.util.regex.Matcher matcher2 = pattern.matcher(nextMonthPrice);

        if (matcher1.find() && matcher2.find()) {
            double price1 = Double.parseDouble(matcher1.group(1));
            double price2 = Double.parseDouble(matcher2.group(1));
            return Math.min(price1, price2);
        }

        throw new IllegalArgumentException("Could not extract prices from both strings");

    }
}
