import {html} from "lit-html";

export default () => html`
<div class="h-full flex flex-col justify-start items-center text-center text-2xl">
    <svg class="status status__red" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 52 52">
        <circle class="status__circle red" cx="26" cy="26" r="25" fill="none" />
        <path class="status__cross" fill="none" d="M16 16 36 36 M36 16 16 36" />
    </svg>
    <span class="mt-5">FAILED</span>
</div>
`