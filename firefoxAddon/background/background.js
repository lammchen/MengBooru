let selectedMediaUrl = null;
let dashboardWindow = null;

browser.contextMenus.create({
  id: "save-to-mengbooru",
  title: "Sauvegarder dans MengBooru",
  contexts: ["image", "video"]
});

browser.contextMenus.create({
  id: "open-mengbooru-dashboard",
  title: "Ouvrir le dashboard MengBooru",
  contexts: ["browser_action"]
});

// clics du menu contextuel
browser.contextMenus.onClicked.addListener((info, tab) => {
  if (info.menuItemId === "save-to-mengbooru") {
    selectedMediaUrl = info.srcUrl;
    
    browser.windows.create({
      url: browser.runtime.getURL("popup/popup.html"),
      type: "popup",
      width: 400,
      height: 500
    });
  } else if (info.menuItemId === "open-mengbooru-dashboard") {
    openDashboard();
  }
});

// TODO : ce gestionnaire n'est pas appelé selon le manifest
browser.browserAction.onClicked.addListener(() => {
  openDashboard();
});

// Ouvrir le dashboard
function openDashboard() {
  if (dashboardWindow) {
    browser.windows.update(dashboardWindow.id, { focused: true });
    return;
  } // ouvert
  
  browser.windows.create({
    url: browser.runtime.getURL("dashboard/dashboard.html"),
    type: "popup",
    width: 800,
    height: 600
  }).then(windowInfo => {
    dashboardWindow = windowInfo;
    
    browser.windows.onRemoved.addListener(function handleRemoved(windowId) {
      if (windowId === dashboardWindow.id) {
        dashboardWindow = null;
        browser.windows.onRemoved.removeListener(handleRemoved);
      } // fermeture
    });
  });
}

browser.runtime.onMessage.addListener((message, sender, sendResponse) => {
  if (message.action === "getSelectedMedia") {
    sendResponse({ mediaUrl: selectedMediaUrl });
  } else if (message.action === "openDashboard") {
    openDashboard();
    sendResponse({ success: true });
  }
  
  return true;
});

// Communication entre onglets
browser.tabs.onUpdated.addListener((tabId, changeInfo, tab) => {
  if (changeInfo.status === 'complete' && tab.url.match(/^(http|https):\/\//)) {
    browser.tabs.executeScript(tabId, {
      file: "/content/content.js"
    }).catch(error => {
      console.error(`Impossible d'injecter le script de contenu: ${error}`);
    });
  }
});

browser.runtime.onInstalled.addListener(() => {
  console.log("Extension MengBooru installée");
});