// Script injecté dans les pages web pour permettre la communication avec l'extension

// Écouter les messages du background script
browser.runtime.onMessage.addListener((message, sender, sendResponse) => {
    if (message.action === "getSelectedMedia") {
      // Nous n'avons rien à faire ici car le background script a déjà l'URL
      // Nous répondons juste pour que le message soit traité
      sendResponse({});
    }
    
    // Nécessaire pour les réponses asynchrones
    return true;
  });
  
  // Injecter des raccourcis clavier ou autres fonctionnalités si nécessaire
  // Par exemple, on pourrait ajouter un raccourci pour enregistrer l'image sous le curseur
  