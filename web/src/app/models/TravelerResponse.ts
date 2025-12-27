export class TravelerResponse {
  id: string
  name: string
  email:string
  phone: string
  photoUrl: string
  nick: string
  about: string
  birth: string
  age: number

  constructor(  id: string,
  name: string,
  email:string,
  phone: string,
  photoUrl: string,
  nick: string,
  about: string,
  birth: string,
  age: number){
    this.id = id;
    this.name=name;
    this.email=email;
    this.phone=phone;
    this.photoUrl=photoUrl;
    this.nick=nick;
    this.about=about;
    this.birth = birth;
    this.age = age;
  }

}
