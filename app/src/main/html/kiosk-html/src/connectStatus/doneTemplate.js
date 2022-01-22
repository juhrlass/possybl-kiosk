import {html} from "lit-html";

export default () => html`
<div class="h-full flex flex-col justify-start items-center text-2xl text-center">
    <svg class="status" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 52 52">
        <circle class="status__circle green" cx="26" cy="26" r="25" fill="none"/>
        <path class="status__check" fill="none" d="M14.1 27.2l7.1 7.2 16.7-16.8"/>
    </svg>
    <span class="mt-5">CONNECTED</span>
</div>
`