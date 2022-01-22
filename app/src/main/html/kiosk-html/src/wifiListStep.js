import {html} from "lit-html";
import wifiListItem from "./wifiListItem";

export default (ssidArray, wifiSelectedHandler) => {
return (ssidArray.length > 0 ? html`
  <div class="h-full flex flex-col relative">
      <h3 class="font-bold text-2xl text-left mb-5">Select a Wifi to connect to:</h3>
      <div class="flex flex-col items-center justify-items-start w-full overflow-y-auto">
          ${ssidArray.map((ssid) => wifiListItem(ssid, wifiSelectedHandler))}
      </div>
  </div>
` :
html`
  <div class="h-full flex flex-col relative">
      <h3 class="font-bold text-2xl text-left mb-5">Select a Wifi to connect to:</h3>
      <h2 class="text-left text-xl">No networks were found! Please check and scan again.</h2>
  </div>
`);
}
