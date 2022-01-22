import {html, nothing} from "lit-html";
import arrowRightSVG from '../public/arrow-right.svg';
import wifiSVG from '../public/wifi.svg';

export default (ssid, wifiSelectedHandler, isConnected) => html`
<div class="overflow-hidden border-b-2 border-gray-800 border-opacity-25 w-full flex-none flex flex-row justify-between items-center text-2xl"
     @click=${() => wifiSelectedHandler(ssid)}>
   <div>
        ${ssid}
   </div>
   <div @click=${() => wifiSelectedHandler(ssid)} class="flex flex-row">
       <div class="mr-5" id=${ssid}>
            ${isConnected ? html`<img src=${wifiSVG} class="w-2-" />` : nothing}
       </div>
      <img src=${arrowRightSVG} class="mx-2 w-2"/>
   </div>
</div>
`