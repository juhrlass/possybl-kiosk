import {html} from "lit-html";
import arrowRightSVG from '../public/arrow-right.svg';
import arrowLeftSVG from '../public/arrow-left.svg';

export default (ssid, goBackClickHandler, connectClickHandler) => html`
<div class="w-full flex flex-col text-2xl">
    <div class="mb-4">
        <label class="block text-2xl text-left font-bold mb-2" for="modalCurrentPassphrase">
            Enter the Passphrase for <span class="font-light">${ssid}</span>:
        </label>
        <input class="form-input w-full py-2 px-3 text-grey-darker border-0 border-b-2 border-gray-800 focus:ring-0 focus:outline-none bg-transparent" id="modalCurrentPassphrase" type="password" placeholder="">
    </div>
    <div class="flex flex-row justify-between text-lg">
         <div class="flex flex-row place-items-center" @click=${goBackClickHandler}>
            <span><img src=${arrowLeftSVG} class="w-2 mx-2"/></span>
            <span>Previous</span>
        </div>
        <div class="flex flex-row place-items-center" @click=${connectClickHandler}>
            <span>Next</span>
            <span><img src=${arrowRightSVG} class="w-2 mx-2"/></span>
        </div>
    </div>
</div>
`