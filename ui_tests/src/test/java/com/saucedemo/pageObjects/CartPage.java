package com.saucedemo.pageObjects;

import com.saucedemo.ModernBasePage;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

import static com.saucedemo.CucumberHooks.getContext;
import static com.saucedemo.CucumberHooks.getDriver;
import static com.saucedemo.helpers.ElementsInteraction.*;
import static com.saucedemo.pageObjects.GlobalSteps.getFormattedTableRows;
import static org.openqa.selenium.support.ui.ExpectedConditions.titleIs;
import static org.testng.Assert.assertEquals;

public class CartPage extends ModernBasePage {
    private static final Logger LOG = LoggerFactory.getLogger(CartPage.class);
    private static final By CHECKOUT_BUTTON = By.className("checkout_button");
    private static final By HEADER_FIELD = By.className("subheader");
    private static final By CART_ITEM_CONTAINER = By.className("cart_item");
    private static final By CART_ITEM_QUANTITY_FIELD = By.className("cart_quantity");
    private static final By CART_ITEM_ITEM_NAME_FIELD = By.className("inventory_item_name");
    private static final By CART_ITEM_DESCRIPTION_FIELD = By.className("inventory_item_desc");
    private static final By CART_ITEM_PRICE_FIELD = By.className("inventory_item_price");
    private static final By REMOVE_CART_ITEM_BUTTON = By.xpath("//button[.='REMOVE']");

    public CartPage() {
    }

    public CartPage(boolean takeScreenShot) {
        super(takeScreenShot);
    }

    public void isPageOpened() {
        getWait().until(titleIs("Swag Labs"));
        assertElementText(HEADER_FIELD, "Your Cart");
    }

    @Given("^I am on Recruiters start page$")
    public void verifyIsPageOpened() {
        waitForOpen();
    }


    private static By getCardFieldLocator(String templateFieldName) {
        switch (templateFieldName) {
            case "quantity":
                return CART_ITEM_QUANTITY_FIELD;
            case "product":
                return CART_ITEM_ITEM_NAME_FIELD;
            case "description":
                return CART_ITEM_DESCRIPTION_FIELD;
            case "price":
                return CART_ITEM_PRICE_FIELD;
            default:
                throw new IllegalArgumentException("Unsupported field name " + templateFieldName);
        }
    }

    @When("^The following products are (available|not available) on Cart page:$")
    public void verifyJobsPresenceInSearchResults(String availability, DataTable jobsResultsData) {
        List<Map<String, String>> products = jobsResultsData.asMaps(String.class, String.class);
        List<WebElement> cartItems = getDriver().findElements(CART_ITEM_CONTAINER);
        LOG.debug("Original rows ({}):\n{}", cartItems.size(), getFormattedTableRows(cartItems));
        for (Map<String, String> product : products) {
            List<WebElement> rows = getFilteredByJobDataJobCards(product);
            String actualAvailability = rows.isEmpty() ? "not available" : "available";
            assertEquals(actualAvailability, availability,
                    String.format("'%s' should be '%s' in\n%s\nContext:\n%s",
                            product,
                            availability,
                            getFormattedTableRows(cartItems),
                            getContext().getDataStore()));
        }
    }

    @When("^I click 'Checkout' button on Cart page$")
    public CheckoutYourInformationPage clickCheckout() {
        click(CHECKOUT_BUTTON);
        return new CheckoutYourInformationPage();
    }

    public List<WebElement> getFilteredByJobDataJobCards(final Map<String, String> product) {
        List<WebElement> cartItems = getDriver().findElements(CART_ITEM_CONTAINER);
        LOG.debug("Original cart items ({}):\n{}", cartItems.size(), getFormattedTableRows(cartItems));
        List<WebElement> rows = new ArrayList<>(cartItems);
        String lastCheckedColumn = null;
        for (String property : product.keySet()) {
            By locator = getCardFieldLocator(property);
            String value = product.get(property);
            if (rows.isEmpty()) {
                LOG.info("Zero results after filtration by {}", lastCheckedColumn);
                break;
            } else {
                rows = rows.stream()
                        .filter(item -> (item.findElement(locator).getText()).equals(value))
                        .collect(Collectors.toList());
            }
            LOG.info("{} remains after filtration by '{}'='{}':\n{}", rows.size(), property, value,
                    getFormattedTableRows(rows));
            lastCheckedColumn = property;
        }
        return rows;
    }
}