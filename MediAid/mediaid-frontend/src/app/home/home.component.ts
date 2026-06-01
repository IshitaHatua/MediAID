import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../core/services/auth.service';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './home.component.html',
  styleUrl: './home.component.css'
})
export class HomeComponent implements OnInit {
  constructor(private auth: AuthService, private router: Router) {}

  ngOnInit() {
    if (this.auth.isLoggedIn()) this.router.navigate([this.auth.getDashboardRoute()]);
  }

  features = [
    { title: 'Easy Registration',     desc: 'Sign up in minutes. Create your citizen profile and upload documents digitally.' },
    { title: 'Scheme Discovery',      desc: 'Browse available government healthcare schemes and check your eligibility criteria.' },
    { title: 'One-Click Enrollment',  desc: 'Enroll in healthcare programs with a streamlined single application process.' },
    { title: 'Digital Claim Filing',  desc: 'Submit healthcare claims online, attach supporting documents, and track approval status.' },
    { title: 'Disbursement Tracking', desc: 'Monitor approved benefits and direct bank account disbursements with full transaction history.' },
    { title: 'Audit & Compliance',    desc: 'Full audit trail for every action. Our compliance engine ensures transparency and accountability.' },
  ];

  steps = [
    { title: 'Register & Verify',    desc: 'Create your account and complete profile verification with an officer.' },
    { title: 'Explore Schemes',      desc: 'Browse government healthcare schemes that match your profile and needs.' },
    { title: 'Enroll & File Claims', desc: 'Enroll in eligible schemes and submit claims with supporting documentation.' },
    { title: 'Receive Benefits',     desc: 'Approved disbursements are transferred directly to your bank account.' },
  ];

  pillars = [
    { title: 'Fully Paperless',      desc: 'End-to-end digital process. No physical forms or office visits.' },
    { title: 'Real-Time Tracking',   desc: 'Live application status and instant updates.' },
    { title: 'Direct Bank Transfer', desc: 'Disbursements go straight to your bank account.' },
    { title: 'Secure & Audited',     desc: 'Every action is logged and reviewed for compliance.' },
  ];

  footerLinks: { title: string; links: { label: string; route?: string; href?: string }[] }[] = [
    {
      title: 'Portal',
      links: [
        { label: 'Home',     route: '/' },
        { label: 'Sign In',  route: '/auth/login' },
        { label: 'Register', route: '/auth/register' },
      ]
    },
    {
      title: 'Features',
      links: [
        { label: 'Scheme Enrollment', href: '#features' },
        { label: 'Claim Filing',      href: '#features' },
        { label: 'Disbursements',     href: '#features' },
      ]
    },
    {
      title: 'About',
      links: [
        { label: 'Our Mission', href: '#about' },
        { label: 'How It Works', href: '#how-it-works' },
      ]
    },
  ];
}
