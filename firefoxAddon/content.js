browser.runtime.onMessage.addListener((message, sender, sendResponse) => {
    if (message.action === "getSelectedMedia") {


      sendResponse({});
    }
    
    return true;
  });
  