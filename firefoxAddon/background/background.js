// Variables globales
let selectedMediaUrl = null;
let dashboardWindow = null;

// Créer les entrées de menu contextuel
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

// Gérer les clics sur le menu contextuel
browser.contextMenus.onClicked.addListener((info, tab) => {
  if (info.menuItemId === "save-to-mengbooru") {
    // Stocker l'URL du média sélectionné
    selectedMediaUrl = info.srcUrl;
    
    // Ouvrir le popup dans une fenêtre
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

// Ajouter un écouteur pour le clic sur l'icône de l'extension
browser.browserAction.onClicked.addListener(() => {
  // Si le popup est défini dans le manifest, ce gestionnaire ne sera pas appelé
  // mais nous l'ajoutons quand même au cas où nous voudrions remplacer le popup par le dashboard
  openDashboard();
});

// Fonction pour ouvrir le dashboard
function openDashboard() {
  // Si le dashboard est déjà ouvert, le mettre au premier plan
  if (dashboardWindow) {
    browser.windows.update(dashboardWindow.id, { focused: true });
    return;
  }
  
  // Sinon, ouvrir une nouvelle fenêtre avec le dashboard
  browser.windows.create({
    url: browser.runtime.getURL("dashboard/dashboard.html"),
    type: "popup",
    width: 800,
    height: 600
  }).then(windowInfo => {
    dashboardWindow = windowInfo;
    
    // Écouter la fermeture de la fenêtre
    browser.windows.onRemoved.addListener(function handleRemoved(windowId) {
      if (windowId === dashboardWindow.id) {
        dashboardWindow = null;
        browser.windows.onRemoved.removeListener(handleRemoved);
      }
    });
  });
}

// Écouter les messages des scripts de contenu ou du popup
browser.runtime.onMessage.addListener((message, sender, sendResponse) => {
  if (message.action === "getSelectedMedia") {
    sendResponse({ mediaUrl: selectedMediaUrl });
  } else if (message.action === "openDashboard") {
    openDashboard();
    sendResponse({ success: true });
  }
  
  // Nécessaire pour les réponses asynchrones
  return true;
});

// Injecter un script de contenu dans chaque onglet pour permettre la communication
browser.tabs.onUpdated.addListener((tabId, changeInfo, tab) => {
  if (changeInfo.status === 'complete' && tab.url.match(/^(http|https):\/\//)) {
    browser.tabs.executeScript(tabId, {
      file: "/content/content.js"
    }).catch(error => {
      console.error(`Impossible d'injecter le script de contenu: ${error}`);
    });
  }
});

// Ajouter un bouton au popup pour ouvrir le dashboard
browser.runtime.onInstalled.addListener(() => {
  // Cette partie est optionnelle, elle pourrait ajouter un bouton dans la barre d'outils
  console.log("Extension MengBooru installée");
});