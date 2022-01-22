'use strict'

import {html, render} from "lit-html";
import {transition, fade} from "lit-transition";
import wifiListTemplate from "./wifiListStep";
import wifiListItemTemplate from "./wifiListItem";
import loadingTemplate from "./loadingTemplate";
import passphraseTemplate from "./wifiPassphraseStep";
import doneTemplate from "./connectStatus/doneTemplate";
import failedTemplate from "./connectStatus/failedTemplate";
export default class WifiWizard {

    constructor() {
        this.selectedSSID = "";
        this.currentStepTemplate = null;
        this.interval = null;
        this.wizardElement = document.getElementById("wizard");
    }

    init() {
        this.updateWizardView(loadingTemplate());
        this.scanWifi();
    }

    hasInternetConnection() {
        console.log("Connection check started");
        var connected = false;
        if (typeof POSSYBL_KIOSK !== "undefined") {
          var connectedSSID = POSSYBL_KIOSK.getActiveWifiSSID();
          if (connectedSSID) {
            console.log("It seems we are connected to the internet over " + connectedSSID);
            connected = true;
          } else {
            console.log("It seems we are NOT connected to the internet!");
          }
        } else {
          console.log(
            "No Android Kiosk environment -> Assuming we are connected"
          );
          connected = true;
        }
        return connected;
    }

    setSSIDs(jsonSSIDs) {
        console.log(jsonSSIDs);
        const ssidObject = JSON.parse(jsonSSIDs);
        const ssidsArray = ssidObject["ssids"];
        const connectHandler = (ssid) => this.startConnect(ssid);
        this.updateWizardView(wifiListTemplate(ssidsArray, connectHandler));
        this.showWifiScanButton();
    }

    startConnect(ssid) {
        console.log(`Connecting to ${ssid}`);
        this.selectedSSID = ssid;
        if (typeof POSSYBL_KIOSK !== "undefined") {
            POSSYBL_KIOSK.stopWifiScan();
        } else {
            console.log(
                "No Android Kiosk environment -> Nothing to do"
            );
        }

        const goBackHandler = () => this.resetView();
        const connectClickHandler = (e) => this.doConnect(e);
        this.updateWizardView(passphraseTemplate(ssid, goBackHandler, connectClickHandler));
    }

    doConnect(e) {
        var passphraseElement = document.getElementById(
          "modalCurrentPassphrase"
        );

        var currentPassphrase = passphraseElement.value;
        passphraseElement.value = "";
        this.updateWizardView(loadingTemplate("CONNECTING ..."));
        if (typeof POSSYBL_KIOSK !== "undefined") {
          POSSYBL_KIOSK.connectToWifi(this.selectedSSID, currentPassphrase);
        } else {
          console.log(
            "No Android Kiosk environment -> Just logging ssid: " +
              this.selectedSSID +
              " and passphrase: " +
              currentPassphrase
          );
        }
        this.checkConnectionPeriodically();
    }

    checkConnectionPeriodically() {
        this.interval = setInterval(() => {
            if (this.hasInternetConnection()) {
                clearInterval(this.interval);
                this.updateWizardView(doneTemplate());
            }
        }, 2000);
        setTimeout(() => {
          if (!this.hasInternetConnection()) {
            this.updateWizardView(failedTemplate());
          }
          clearInterval(this.interval);
        }, 30000);
    }

    scanWifi() {
        console.log("Scan Wifi started");

        if (typeof POSSYBL_KIOSK !== "undefined") {
          POSSYBL_KIOSK.scanWifi();
        } else {
          console.log("No Android Kiosk environment -> Just faking some SSIDS");
          this.setSSIDs('{"ssids":["fake ssid 1","fake ssid 2", "fake ssid 3", "fake ssid 4", "fake ssid 5"]}');
        }
    }

    setWifiStatus(status) {
        console.log(status);
        this.updateWizardView(connectTemplate(status));
    }

    resetView() {
        window.location.href = "";
    }

    updateWizardView(newTemplate, shouldTransition) {
        this.currentStepTemplate = newTemplate;
        render(this.currentStepTemplate, this.wizardElement);
    }

    goBackToDefaultURL() {
        let defaultUrl = "www.google.com";
        if (typeof POSSYBL_KIOSK !== "undefined") {
            defaultUrl = POSSYBL_KIOSK.getDefaultURL()
        }
        window.location = defaultUrl;
    }

    showWifiScanButton() {
        document.getElementById('wifiScanButton').classList.remove('hidden');
    }
}
